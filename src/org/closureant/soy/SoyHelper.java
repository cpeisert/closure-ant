/*
 * Copyright (C) 2012 Christopher Peisert. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.closureant.soy;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.Files;
import com.google.inject.Injector;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.restricted.PrimitiveData;
import com.google.template.soy.jssrc.SoyJsSrcOptions;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.msgs.SoyMsgBundleHandler;
import com.google.template.soy.msgs.SoyMsgPlugin;
import com.google.template.soy.shared.SoyCssRenamingMap;
import com.google.template.soy.shared.SoyGeneralOptions;
import com.google.template.soy.tofu.SoyTofu;
import com.google.template.soy.xliffmsgplugin.XliffMsgPlugin;
import com.google.template.soy.xliffmsgplugin.XliffMsgPluginModule;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.closureant.util.ClosureBuildUtil;

/**
 * Helper object to gather settings for common Soy operations such as compiling
 * Soy files to JavaScript files, rendering templates, extracting messages for
 * translation, and generating template parse info to make working with
 * templates in Java code less error-prone.
 *
 * <p>Example Usage</p>
 *
 * <p><pre>{@code
SoyJsSrcOptions jsSrcOptions = new SoyJsSrcOptions();
jsSrcOptions.setShouldProvideRequireSoyNamespaces(true);

File soyFile = new File("./my_templates.soy");

SoyHelper helper = new SoyHelper.Builder()
    .soyJsSrcOptions(jsSrcOptions)
    .sourceFile(soyFile)
    .build();

SoyMapData params = new SoyMapData("param_name", 42);
String renderedContent = helper.render("my.template", params);
 * }</pre></p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class SoyHelper {

  private final Set<String> activeDelegatePackageNames;
  private final SoyCssRenamingMap cssRenamingMap;
  private final String extractedMessagesSourceLocale;
  private final Set<String> extractedMessagesTargetLocales;
  private final Injector injector;
  private final Set<SoyMsgBundle> msgBundleSet;
  private final String parseInfoJavaClassname; // SoyFileSet.generateParseInfo()
  private final String parseInfoJavaPackage; // SoyFileSet.generateParseInfo()
  private final SoyFileSet soyFileSet;
  // List of Soy source files topologically sorted based on their dependencies.
  private final List<SoySourceFile> soySourceManifest;
  private final SoyTofu soyTofu;

  /**
   * Private constructor to initialize {@link SoyHelper} from builder.
   * 
   * @param builder a {@link SoyHelper} builder
   */
  private SoyHelper(Builder builder) {
    this.activeDelegatePackageNames = builder.activeDelegatePackageNames;
    this.cssRenamingMap = builder.cssRenamingMap;
    this.extractedMessagesSourceLocale = builder.extractedMessagesSourceLocale;
    this.extractedMessagesTargetLocales =
        builder.extractedMessagesTargetLocales;
    this.injector = builder.injector;
    this.msgBundleSet = builder.msgBundleSet;
    this.parseInfoJavaClassname = builder.parseInfoJavaClassname;
    this.parseInfoJavaPackage = builder.parseInfoJavaPackage;

    boolean allowExternalCalls = builder.generalOptions.allowExternalCalls();

    SoyFileSet.Builder sfsBuilder = new SoyFileSet.Builder()
        .setAllowExternalCalls(allowExternalCalls)
        .setCompileTimeGlobals(builder.generalOptions.getCompileTimeGlobals())
        .setCssHandlingScheme(builder.generalOptions.getCssHandlingScheme());

    for (SoySourceFile soySourceFile : builder.soySourceFiles) {
      sfsBuilder.add(new File(soySourceFile.getAbsolutePath()));
    }
    this.soyFileSet = sfsBuilder.build();
    this.soyTofu = this.soyFileSet.compileToTofu();

    Map<String, SoySourceFile> provideToSource =
        ClosureBuildUtil.createMapOfProvideToSource(builder.soySourceFiles);

    Multimap<SoySourceFile, SoySourceFile> sourceToDependencies = null;

    // We want to ignore missing dependencies in the SoyHelper constructor,
    // since 'soy', 'soy.StringBuilder', 'soy.esc', 'soydata', and friends
    // are provided at render time (in Java they are implicitly provided, and
    // in JavaScript they are provided by either soyutils.js or
    // soyutils_usegoog.js).
    sourceToDependencies = ClosureBuildUtil.
        createSourceToDependenciesIgnoringMissingDeps(
            builder.soySourceFiles, provideToSource);

    this.soySourceManifest = ClosureBuildUtil
        .topologicalStableSortKahnAlgorithm(
            Lists.newArrayList(builder.soySourceFiles), sourceToDependencies);
  }

  /**
   * Extracts all messages into a SoyMsgBundle.
   *
   * @return a SoyMsgBundle containing all the extracted messages.
   * @throws SoySyntaxException if a syntax error is found
   */
  public SoyMsgBundle extractMessagesToSoyMsgBundle() {
    return this.soyFileSet.extractMsgs();
  }

  /**
   * Extracts all messages to the file format defined by the specified Soy
   * message module, which defaults to {@link XliffMsgPluginModule}. The
   * source and target locales are specified using {@link SoyHelper.Builder}.
   *
   * <p>For each target locale a map entry is created as follows:</p>
   *
   * <p>{@literal <key=locale, value=messagesFileContent>}</p>
   *
   * <p>If there are no target locales specified, the returned map will
   * contain one entry with the default locale "en". However, the actual file
   * content will have the target locale set to empty.</p>
   *
   * @return a Map of target locales to the message file format specified by
   *     the Soy message module (defaults to {@link XliffMsgPluginModule})
   * @throws SoySyntaxException if a syntax error is found
   */
  public ImmutableMap<String, String> extractMessagesForEachTargetLocale() {
    SoyMsgBundleHandler.OutputFileOptions options =
        new SoyMsgBundleHandler.OutputFileOptions();
    options.setSourceLocaleString(this.extractedMessagesSourceLocale);

    SoyMsgPlugin msgPlugin = this.injector.getInstance(SoyMsgPlugin.class);
    SoyMsgBundle msgBundle = this.soyFileSet.extractMsgs();
    ImmutableMap.Builder<String, String> localeToMessagesFileContent =
        ImmutableMap.builder();

    for (String targetLocale : this.extractedMessagesTargetLocales) {
      options.setTargetLocaleString(targetLocale);
      String fileContent =
          msgPlugin.generateExtractedMsgsFile(msgBundle, options).toString();
      localeToMessagesFileContent.put(targetLocale, fileContent);
    }

    if (this.extractedMessagesTargetLocales.isEmpty()) {
      String fileContent =
          msgPlugin.generateExtractedMsgsFile(msgBundle, options).toString();
      localeToMessagesFileContent.put("en", fileContent);
    }

    return localeToMessagesFileContent.build();
  }

  /**
   * Generates Java classes containing parse info (param names, template
   * names, meta info). There will be one Java class per Soy file.
   *
   * @return A map from generated file name (of the form
   *     "{@literal <*>SoyInfo.java}") to generated file content.
   */
  public ImmutableMap<String, String> generateParseInfo() {
    return this.soyFileSet.generateParseInfo(this.parseInfoJavaPackage,
        this.parseInfoJavaClassname);
  }

  /**
   * Get the Guice injector initialized with the plugin modules specified in
   * the {@link SoyHelper.Builder}.
   *
   * @return the Guice injector
   */
  public Injector getGuiceInjector() {
    return this.injector;
  }

  /**
   * Immutable list of the Soy source files contained in this {@link
   * SoyHelper}. The sources are sorted in topological order based on their
   * transitive dependencies.
   *
   * @return immutable list of Soy source files topologically sorted
   */
  public ImmutableList<SoySourceFile> getListOfSoySourceFiles() {
    return ImmutableList.copyOf(this.soySourceManifest);
  }

  /**
   * An unmodifiable iterator over the Soy source files contained in this
   * {@link SoyHelper}. The sources are iterated in topological order based
   * on their transitive dependencies.
   *
   * @return unmodifiable iterator over the Soy source files
   */
  public UnmodifiableIterator<SoySourceFile> soySourceFileIterator() {
    return Iterators.unmodifiableIterator(this.soySourceManifest.iterator());
  }

  /**
   * Renders a template with no data using the messages in the original Soy
   * file (that is, any message bundles added to the {@link SoyHelper} are
   * ignored).
   *
   * @param templateName the full name of the template including the namespace
   * @return a new renderer for the given template
   * @see #renderForEachLocale(String, com.google.template.soy.data.SoyMapData,
   *     com.google.template.soy.data.SoyMapData)
   * @see #render(String, com.google.template.soy.data.SoyMapData,
   *     com.google.template.soy.data.SoyMapData)
   */
  public String render(String templateName) {
    return render(templateName, null, null);
  }

  /**
   * Renders a template using the messages in the original Soy file (that is,
   * any message bundles added to the {@link SoyHelper} are ignored).
   *
   * @param templateName the full name of the template including the namespace
   * @param data the data to call the template with. May be {@code null} if
   *     the template has no parameters.
   * @return a new renderer for the given template
   * @see #renderForEachLocale(String, com.google.template.soy.data.SoyMapData,
   *     com.google.template.soy.data.SoyMapData)
   * @see #render(String, com.google.template.soy.data.SoyMapData,
   *     com.google.template.soy.data.SoyMapData)
   */
  public String render(String templateName, SoyMapData data) {
    return render(templateName, data, null);
  }

  /**
   * Renders a template using the messages in the original Soy file (that is,
   * any message bundles added to the {@link SoyHelper} are ignored).
   *
   * @param templateName the full name of the template including the namespace
   * @param data the data to call the template with. May be {@code null} if
   *     the template has no parameters.
   * @param ijData the injected data to call the template with. May be
   *     {@code null} if not used.
   * @return a new renderer for the given template
   * @see #renderForEachLocale(String, com.google.template.soy.data.SoyMapData,
   *     com.google.template.soy.data.SoyMapData)
   */
  public String render(String templateName, SoyMapData data,
      SoyMapData ijData) {
    SoyTofu.Renderer renderer = newSoyTofuRenderer(templateName, data, ijData);
    return renderer.render();
  }

  /**
   * Renders a template for each {@link SoyMsgBundle} that has been added to
   * the {@link SoyHelper} where each localized rendering is returned as a
   * separate map entry {@literal <key=locale, value=renderedContent>}. If no
   * message bundles are set, the returned map will contain one entry with
   * the default locale "en".
   *
   * @param templateName the full name of the template including the namespace
   * @param data the data to call the template with. May be {@code null} if
   *     the template has no parameters.
   * @param ijData the injected data to call the template with. Can be
   *     {@code null} if not used.
   * @return a map from locale string (returned by {@link
   *     com.google.template.soy.msgs.SoyMsgBundle#getLocaleString()}) to the
   *     rendered output for the corresponding locale
   */
  public ImmutableMap<String, String> renderForEachLocale(String templateName,
      SoyMapData data, SoyMapData ijData) {
    SoyTofu.Renderer renderer = newSoyTofuRenderer(templateName, data, ijData);
    ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();

    for (SoyMsgBundle msgBundle : this.msgBundleSet) {
      String locale = msgBundle.getLocaleString();
      String renderedContent = renderer.setMsgBundle(msgBundle).render();
      mapBuilder.put(locale, renderedContent);
    }

    if (this.msgBundleSet.isEmpty()) {
      mapBuilder.put("en", renderer.render());
    }
    return mapBuilder.build();
  }

  /**
   * Constructs a new {@link SoyTofu.Renderer} based on the CSS renaming map,
   * any active delegate packages specified, the template name, template data,
   * and injected data.
   *
   * @param templateName the full name of the template including the namespace
   * @param data the data to call the template with. May be {@code null} if
   *     the template has no parameters.
   * @param ijData the injected data to call the template with. Can be
   *     {@code null} if not used.
   * @return a {@link SoyTofu.Renderer} for the specified template
   */
  private SoyTofu.Renderer newSoyTofuRenderer(String templateName,
      SoyMapData data, SoyMapData ijData) {
    SoyTofu.Renderer renderer = this.soyTofu.newRenderer(templateName);
    if (this.cssRenamingMap != null) {
      renderer.setCssRenamingMap(this.cssRenamingMap);
    }
    renderer.setActiveDelegatePackageNames(this.activeDelegatePackageNames);
    if (data != null) {
      renderer.setData(data);
    }
    if (ijData != null) {
      renderer.setIjData(ijData);
    }
    return renderer;
  }


  //----------------------------------------------------------------------------


  /**
   * Builder for a {@link SoyHelper}.
   */
  public static class Builder {

    private final Set<String> activeDelegatePackageNames;
    private ClassLoader classLoader;
    // Temporary container used to gather compile-time globals from files,
    // maps, string literals, and resources.
    private Map<String, PrimitiveData> compileTimeGlobalsMap;
    private SoyCssRenamingMap cssRenamingMap;
    private String extractedMessagesSourceLocale;
    private final Set<String> extractedMessagesTargetLocales;
    private SoyGeneralOptions generalOptions;
    private Injector injector;
    private SoyJsSrcOptions jsSrcOptions;
    private String messagePluginModule;
    private final Set<SoyMsgBundle> msgBundleSet;
    private final Map<String, SoySourceFile> namespaceToSoySourceFile;
    private String parseInfoJavaClassname;
    private String parseInfoJavaPackage;
    private final List<String> pluginModules;
    private boolean soyHelperBuilt;
    private final Set<SoySourceFile> soySourceFiles;
    private final Set<File> tempSourceFiles;
    private final Set<URL> tempSourceURLs;
    private final Set<File> translationFiles;

    public Builder() {
      this.activeDelegatePackageNames = Sets.newHashSet();
      this.classLoader = null;
      this.compileTimeGlobalsMap = Maps.newHashMap();
      this.cssRenamingMap = null;
      this.extractedMessagesSourceLocale = null;
      this.extractedMessagesTargetLocales = Sets.newHashSet();

      this.generalOptions = new SoyGeneralOptions();
      this.generalOptions.setAllowExternalCalls(false);
      this.generalOptions.setCssHandlingScheme(
          SoyGeneralOptions.CssHandlingScheme.LITERAL);

      this.injector = null;
      this.jsSrcOptions = null;
      this.messagePluginModule = null;
      this.msgBundleSet = Sets.newHashSet();
      this.namespaceToSoySourceFile = Maps.newHashMap();
      this.parseInfoJavaClassname = null;
      this.parseInfoJavaPackage = null;
      this.pluginModules = Lists.newArrayList();
      this.soyHelperBuilt = false;
      this.soySourceFiles = Sets.newHashSet();
      this.tempSourceFiles = Sets.newHashSet();
      this.tempSourceURLs = Sets.newHashSet();
      this.translationFiles = Sets.newHashSet();
    }

    /**
     * Sets the active delegate package names. See <a target="_blank"
     * href="https://developers.google.com/closure/templates/docs/commands#deltemplate">
     * deltemplate, delcall, delpackage</a>.
     *
     * @param activeDelegatePackageNames Collection of active delegate
     *     package names
     * @return this {@link Builder}
     */
    public Builder activeDelegatePackageNames(
        Collection<String> activeDelegatePackageNames) {
      this.activeDelegatePackageNames.addAll(activeDelegatePackageNames);
      return this;
    }

    /**
     * Sets whether to allow external calls (calls to undefined templates).
     *
     * @param allowExternalCalls The value to set.
     * @return this {@link Builder}
     */
    public Builder allowExternalCalls(boolean allowExternalCalls) {
      this.generalOptions.setAllowExternalCalls(allowExternalCalls);
      return this;
    }

    /**
     * Creates a new {@link SoyHelper}.
     *
     * @return a {@link SoyHelper}
     * @throws IOException if there is an error reading a translations file
     * @throws IllegalStateException if {@link #build()} already called
     * @throws MultipleDeclarationException if a namespace is declared in more
     *     than one Soy file
     */
    public SoyHelper build() throws IOException {
      if (this.soyHelperBuilt) {
        throw new IllegalStateException("may not call build() more than "
            + "once due to potential violation of class invariants");
      }

      this.generalOptions.setCompileTimeGlobals(this.compileTimeGlobalsMap);

      if (this.jsSrcOptions == null) {
        this.jsSrcOptions = new SoyJsSrcOptions();
      }
      if (this.messagePluginModule == null) {
        this.messagePluginModule = XliffMsgPluginModule.class.getName();
      }

      this.injector = ExtraSoyUtils.createInjector(this.messagePluginModule,
          this.pluginModules, this.classLoader);

      SoyMsgPlugin msgPlugin = injector.getInstance(SoyMsgPlugin.class);
      for (File file : this.translationFiles) {
        this.msgBundleSet.add(msgPlugin.parseTranslatedMsgsFile(
            Files.toString(file, Charsets.UTF_8)));
      }

      // Now that the fields have been initialized, it is safe to use them
      // to construct SoySourceFiles from raw Soy Files.

      // Since each Soy file is being compiled individually, it is necessary to
      // allow calls to external/undefined templates that will be resolved at
      // render time.
      SoyGeneralOptions optionsForFiles = this.generalOptions.clone();
      optionsForFiles.setAllowExternalCalls(true);

      for (File source : this.tempSourceFiles) {
        SoySourceFile soySourceFile = new SoySourceFileImpl(source,
            optionsForFiles, this.jsSrcOptions, this.msgBundleSet,
            this.parseInfoJavaClassname, this.parseInfoJavaPackage);
        internalAddSoySourceFile(soySourceFile);
      }
      for (URL url : this.tempSourceURLs) {
        SoySourceFile soySourceFile = new SoySourceFileImpl(url,
            optionsForFiles, this.jsSrcOptions, this.msgBundleSet,
            this.parseInfoJavaClassname, this.parseInfoJavaPackage);
        internalAddSoySourceFile(soySourceFile);
      }

      this.soyHelperBuilt = true;
      return new SoyHelper(this);
    }

    /**
     * Sets a custom class loader for loading plugin modules. This is useful
     * for including class paths that are defined at runtime.
     *
     * @param classLoader the class loader
     * @return this {@link Builder}
     */
    public Builder classLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
      return this;
    }

    /**
     * Sets a compile-time global to a value, which must be one of the Soy
     * primitive types: null, boolean, integer, float (Java double), or string.
     *
     * <p><b>Note:</b> Soy strings must be single quoted.</p>
     *
     * <p>Example Soy type literals:</p>
     *
     * <p><ul>
     * <li>Soy null: {@code null}</li>
     * <li>Soy boolean: {@code true}</li>
     * <li>Soy integer: {@code 42}</li>
     * <li>Soy float: {@code 0.25}</li>
     * <li>Soy string: {@code 'this is a string'}</li>
     * </ul></p>
     *
     * @param name the name of a compile-time global variable
     * @param value the value of the global
     * @throws com.google.template.soy.base.SoySyntaxException If the value is
     *     not a valid Soy primitive type.
     * @return this {@link Builder}
     */
    public Builder compileTimeGlobal(String name, String value) {
      PrimitiveData data = ExtraSoyUtils.parseStringToPrimitiveData(value);
      this.compileTimeGlobalsMap.put(name, data);
      return this;
    }

    /**
     * Sets the map from compile-time global name to value.
     *
     * <p>The values can be any of the Soy primitive types: null, boolean,
     * integer, float (Java double), or string.
     *
     * @param compileTimeGlobalsMap Map from compile-time global name to value.
     *     The values can be any of the Soy primitive types: null, boolean,
     *     integer, float (Java double), or string.
     * @throws com.google.template.soy.base.SoySyntaxException If one of the
     *     values is not a valid Soy primitive type.
     * @return this {@link Builder}
     */
    public Builder compileTimeGlobals(Map<String, ?> compileTimeGlobalsMap) {
      SoyGeneralOptions options = new SoyGeneralOptions();
      options.setCompileTimeGlobals(compileTimeGlobalsMap);
      this.compileTimeGlobalsMap.putAll(options.getCompileTimeGlobals());
      return this;
    }

    /**
     * Sets the file containing compile-time globals.
     *
     * <p>Each line of the file should have the format
     * <pre>{@literal
     *   <global_name> = <primitive_data>
     * }</pre>
     * where primitive_data is a valid Soy expression literal for a primitive
     * type (null, boolean, integer, float, or string). Empty lines and lines
     * beginning with "//" are ignored. The file should be encoded in UTF-8.
     *
     * <p><b>Note:</b> Soy strings must be single quoted.</p>
     *
     * <p>Example Soy type literals:</p>
     *
     * <p><ul>
     * <li>Soy null: {@code null}</li>
     * <li>Soy boolean: {@code true}</li>
     * <li>Soy integer: {@code 42}</li>
     * <li>Soy float: {@code 0.25}</li>
     * <li>Soy string: {@code 'this is a string'}</li>
     * </ul></p>
     *
     * <p> If you need to generate a file in this format from Java, consider
     * using the utility {@code SoyUtils.generateCompileTimeGlobalsFile()}.</p>
     *
     * @param compileTimeGlobalsFile The file containing compile-time globals.
     * @throws IOException If there is an error reading the compile-time
     *     globals file.
     * @return this {@link Builder}
     */
    public Builder compileTimeGlobals(File compileTimeGlobalsFile)
        throws IOException {
      SoyGeneralOptions options = new SoyGeneralOptions();
      options.setCompileTimeGlobals(compileTimeGlobalsFile);
      this.compileTimeGlobalsMap.putAll(options.getCompileTimeGlobals());
      return this;
    }

    /**
     * Sets the resource file containing compile-time globals. See {@link
     * #compileTimeGlobals(java.io.File)}.
     *
     * @param compileTimeGlobalsResource The resource file containing
     *     compile-time globals.
     * @throws IOException If there is an error reading the compile-time
     *     globals file.
     * @return this {@link Builder}
     */
    public Builder compileTimeGlobals(URL compileTimeGlobalsResource)
        throws IOException {
      SoyGeneralOptions options = new SoyGeneralOptions();
      options.setCompileTimeGlobals(compileTimeGlobalsResource);
      this.compileTimeGlobalsMap.putAll(options.getCompileTimeGlobals());
      return this;
    }

    /**
     * Sets the scheme for handling {@code css} commands.
     *
     * @param cssHandlingScheme The css-handling scheme to set. Defaults to
     *     {@code LITERAL}.
     * @return this {@link Builder}
     */
    public Builder cssHandlingScheme(
        SoyGeneralOptions.CssHandlingScheme cssHandlingScheme) {
      this.generalOptions.setCssHandlingScheme(cssHandlingScheme);
      return this;
    }

    /**
     * Sets the {@link SoyCssRenamingMap} for this build.
     *
     * @param cssRenamingMap the CSS renaming map
     * @return this {@link Builder}
     */
    public Builder cssRenamingMap(SoyCssRenamingMap cssRenamingMap) {
      this.cssRenamingMap = cssRenamingMap;
      return this;
    }

    /**
     * The locale string of the source language (default "en") to use when
     * creating a file of extracted messages.
     *
     * @param sourceLocale locale string of the source language. Defaults
     *     to "en".
     */
    public Builder extractedMessageSourceLocale(String sourceLocale) {
      this.extractedMessagesSourceLocale = sourceLocale;
      return this;
    }

    /**
     * The locale string of a target language (default empty) to use when
     * creating a file of extracted messages. If empty, then the output
     * messages file will not specify a target locale string. Note that this
     * option may not be applicable for certain message plugins (in which case
     * this value will be ignored by the message plugin).
     *
     * @param targetLocale locale string of the target language. Defaults
     *     to empty.
     */
    public Builder extractedMessagesTargetLocale(String targetLocale) {
      this.extractedMessagesTargetLocales.add(targetLocale);
      return this;
    }

    /**
     * Locale strings of target languages (default empty) to use when
     * creating files of extracted messages. If empty, then the output
     * messages file will not specify a target locale string. Note that this
     * option may not be applicable for certain message plugins (in which case
     * this value will be ignored by the message plugin).
     *
     * @param targetLocales locale strings of the target languages. Defaults
     *     to empty.
     */
    public Builder extractedMessagesTargetLocales(
        Collection<String> targetLocales) {
      this.extractedMessagesTargetLocales.addAll(targetLocales);
      return this;
    }

    /**
     * Sets the output Java package and format to use for class names when
     * generating Java classes containing info parsed from template files,
     * including template and parameter names, so that you can avoid the
     * error-prone process of manually typing template and parameter names
     * as strings. There will be one Java class per Soy file.
     *
     * @param javaPackage The Java package for the generated classes.
     * @param javaClassNameSource Source of the generated class names. Must be
     *     one of "filename", "namespace", or "generic".
     * @return this {@link Builder}
     */
    public Builder javaParseInfo(String javaPackage,
        String javaClassNameSource) {
      this.parseInfoJavaClassname = javaClassNameSource;
      this.parseInfoJavaPackage = javaPackage;
      return this;
    }

    /**
     * Add a {@link SoyMsgBundle} containing a full set of messages for some
     * language/locale.
     *
     * @param msgBundle a message bundle containing a full set of messages
     *     for some language/locale
     * @return this {@link Builder}
     */
    public Builder messageBundle(SoyMsgBundle msgBundle) {
      this.msgBundleSet.add(msgBundle);
      return this;
    }

    /**
     * Add a Collection of {@link SoyMsgBundle}, where each {@link
     * SoyMsgBundle} contains a full set of messages for some language/locale.
     *
     * @param msgBundles a Collection of message bundles, where each message
     *     bundle contains a full set of messages for some language/locale
     * @return this {@link Builder}
     */
    public Builder messageBundles(Collection<SoyMsgBundle> msgBundles) {
      this.msgBundleSet.addAll(msgBundles);
      return this;
    }

    /**
     * Specifies the full class name of a Guice module that binds a
     * {@link com.google.template.soy.msgs.SoyMsgPlugin}. If not specified,
     * the default is {@link
     * com.google.template.soy.xliffmsgplugin.XliffMsgPluginModule}, which
     * binds the {@code XliffMsgPlugin}.
     *
     * @param messagePluginModule full class name of the Guice module for the
     *     {@code SoyMsgPlugin}
     * @return this {@link Builder}
     */
    public Builder messagePluginModule(String messagePluginModule) {
      this.messagePluginModule = messagePluginModule;
      return this;
    }

    /**
     * Parse a file containing a full set of translated messages for some
     * language/locale. A {@link SoyMsgBundle} is created for the file.
     *
     * @param file file containing a full set of translated messages for some
     *     language/locale
     * @return this {@link Builder}
     * @throws com.google.template.soy.msgs.SoyMsgException if there was an
     *     error parsing the file content
     */
    public Builder parseTranslationsFromFile(File file) {
      this.translationFiles.add(file);
      return this;
    }

    /**
     * Parse an xliff file containing a full set of translated messages for
     * some language/locale. A {@link SoyMsgBundle} is created for the file.
     *
     * @param xliffFile an xliff file containing a full set of translated
     *     messages for some language/locale
     * @return this {@link Builder}
     * @throws IOException if there is an error processing the xliff file
     * @throws com.google.template.soy.msgs.SoyMsgException if there was an
     *     error parsing the file content
     */
    public Builder parseTranslationsFromXliffFile(File xliffFile)
        throws IOException {
      XliffMsgPlugin msgPlugin = new XliffMsgPlugin();
      this.msgBundleSet.add(msgPlugin.parseTranslatedMsgsFile(
          Files.toString(xliffFile, Charsets.UTF_8)));
      return this;
    }

    /**
     * Parse an xliff resource containing a full set of translated messages for
     * some language/locale. A {@link SoyMsgBundle} is created for the resource.
     *
     * @param xliffResource the resource containing a full set of translated
     *     messages for some language/locale
     * @return this {@link Builder}
     * @throws IOException if there is an error processing the resource
     * @throws com.google.template.soy.msgs.SoyMsgException if there was an
     *     error parsing the resource content
     */
    public Builder parseTranslationsFromXliffResource(URL xliffResource)
        throws IOException {
      SoyMsgBundleHandler msgBundleHandler = new SoyMsgBundleHandler(
          new XliffMsgPlugin());
      SoyMsgBundle msgBundle =
          msgBundleHandler.createFromResource(xliffResource);
      this.msgBundleSet.add(msgBundle);
      return this;
    }

    /**
     * Specifies the full class name of a Guice module for a function or
     * print-directive plugin.
     *
     * @param pluginModule full class name of a Guice module for a function or
     *     print-directive plugin
     * @return this {@link Builder}
     */
    public Builder pluginModule(String pluginModule) {
      this.pluginModules.add(pluginModule);
      return this;
    }

    /**
     * Specifies the full class names of Guice modules for function or
     * print-directive plugins.
     *
     * @param pluginModules full class names of Guice modules for function or
     *     print-directive plugins
     * @return this {@link Builder}
     */
    public Builder pluginModules(Collection<String> pluginModules) {
      this.pluginModules.addAll(pluginModules);
      return this;
    }

    /**
     * Adds a Soy file.
     *
     * @param sourceFile the Soy file
     * @return this {@link Builder}
     * @throws MultipleDeclarationException if a namespace is declared in more
     *     than one Soy file
     */
    public Builder sourceFile(File sourceFile) {
      this.tempSourceFiles.add(sourceFile);
      return this;
    }

    /**
     * Adds a Soy source file.
     *
     * @param soySourceFile the Soy source file
     * @return this {@link Builder}
     * @throws MultipleDeclarationException if a namespace is declared in more
     *     than one Soy file
     */
    public Builder sourceFile(SoySourceFile soySourceFile) {
      internalAddSoySourceFile(soySourceFile);
      return this;
    }

    /**
     * Adds a Soy file specified by a URL.
     *
     * @param url the URL specifying a Soy file
     * @return this {@link Builder}
     * @throws MultipleDeclarationException if a namespace is declared in more
     *     than one Soy file
     */
    public Builder sourceFile(URL url) {
      this.tempSourceURLs.add(url);
      return this;
    }

    /**
     * Adds all the specified Soy files.
     *
     * @param soyFiles the Soy files
     * @return this {@link Builder}
     * @throws MultipleDeclarationException if a namespace is declared in more
     *     than one Soy file
     */
    public Builder sourceFiles(Collection<File> soyFiles) {
      for (File file : soyFiles) {
        this.sourceFile(file);
      }
      return this;
    }

    /**
     * Adds all the specified Soy file URLs.
     *
     * @param soyURLs the Soy file URLs
     * @param ignore Parameter used to differentiate between the method that
     *     accepts a Collection of Files and the method that accepts a
     *     Collection of URLs. See <a target="_blank"
     *     href="http://michid.wordpress.com/2010/05/30/working-around-type-erasure-ambiguities/">
     *     Working around type erasure ambiguities</a>
     * @return this {@link Builder}
     * @throws MultipleDeclarationException if a namespace is declared in more
     *     than one Soy file
     */
    public Builder sourceFiles(Collection<URL> soyURLs, URL... ignore) {
      for (URL url : soyURLs) {
        this.sourceFile(url);
      }
      return this;
    }

    /**
     * Sets the {@link SoyJsSrcOptions} for this build.
     *
     * @param soyJsSrcOptions the JavaScript options to use when compiling
     *     Soy templates to JavaScript
     * @return this {@link Builder}
     */
    public Builder soyJsSrcOptions(SoyJsSrcOptions soyJsSrcOptions) {
      this.jsSrcOptions = soyJsSrcOptions.clone();
      return this;
    }

    /**
     * Adds a new SoySourceFile and ensures that the namespace is not provided
     * by another file.
     *
     * @throws MultipleDeclarationException if the namespace is already declared
     */
    private void internalAddSoySourceFile(SoySourceFile soySourceFile) {
      if (this.namespaceToSoySourceFile.containsKey(
          soySourceFile.getNamespace())) {
        throw new MultipleDeclarationException(soySourceFile.getNamespace(),
            soySourceFile,
            this.namespaceToSoySourceFile.get(soySourceFile.getNamespace()));
      }
      this.soySourceFiles.add(soySourceFile);
      this.namespaceToSoySourceFile.put(soySourceFile.getNamespace(),
          soySourceFile);
    }
  }
}
