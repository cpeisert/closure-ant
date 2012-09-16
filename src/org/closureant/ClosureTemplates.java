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

package org.closureant;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.JsonParseException;
import com.google.template.soy.jssrc.SoyJsSrcOptions;
import com.google.template.soy.shared.SoyGeneralOptions;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import org.closureant.soy.ActiveDelegatePackageList;
import org.closureant.soy.JavaParseInfo;
import org.closureant.soy.SoyJsSrcOptionsAntType;
import org.closureant.soy.SoySourceFile;
import org.closureant.soy.TemplateRenderOptions;
import org.closureant.soy.TranslationOptions;
import org.closureant.util.AntUtil;
import org.closureant.types.ClassNameList;
import org.closureant.types.NameValuePair;
import org.closureant.css.CssRenamingMap;
import org.closureant.soy.SoyHelper;

/**
 * Ant task for Closure Templates including: rendering localized Soy
 * templates, compiling Soy templates to JavaScript, extracting messages for
 * translation, and generating parse information to make working with
 * templates in Java less error prone.
 *
 * TODO(cpeisert): Add documentation for Windows users advising to keep project
 * files on the C drive. Also advise that paths should be sanitized/normalized
 * as described below.
 *
 <!--
 On Windows, the JavaScript escape character '\' is used a file separator.
 In order to keep the file paths intact when passed as JSON, ensure that
 '/' is used as the file separator irrespective of platform. In addition,
 the Closure Templates URI sanitizer does not recognize standard file URIs
 such as "file:///c:/Users/smith". However, on windows a file path such
 as "/Users/smith" will be recognized as "C:\Users\smith". If using
 Windows, it is recommended to keep project files on the C drive.
 -->
 <macrodef name="sanitize-path">
   <attribute name="property" />
   <attribute name="path-property" />

   <sequential>
     <pathconvert property="@{property}" dirsep="/">
       <propertyresource name="@{path-property}" />
       <regexpmapper from="^[cC]:(.*)" to="\1" />
     </pathconvert>
   </sequential>
 </macrodef>

 <property name="json-in-style.soy.compiled.js_UNSANITIZED"
     location="${json-in-style.soy-output.dir}/main.compiled.js" />
 <sanitize-path property="json-in-style.soy.compiled.js"
     path-property="json-in-style.soy.compiled.js_UNSANITIZED" />
 *
 *
 *
 *
 * The default task name is {@code soy} as defined in "task-definitions.xml".
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ClosureTemplates extends Task {

  private final SoyHelper.Builder soyHelperBuilder;

  // Attributes not already part of SoyJsSrcOptionsAntType and SoyHelper.Builder
  private String jsOutputPathFormat; // Set in the nested SoyToJsCompileOptions
  private Path soyPluginClasspath;

  // Nested elements
  private final Set<String> activeDelegatePackageNames;
  private JavaParseInfo javaParseInfo;
  private SoyJsSrcOptions jsSrcOptions;
  private final List<String> pluginModules;
  private final List<FileSet> soyFileSets;
  private final List<TemplateRenderOptions> templatesToBeRendered;
  private TranslationOptions translationOptions;

  /**
   * Constructs a new SoyToJsSrcCompiler Ant task.
   */
  public ClosureTemplates() {
    this(null);
  }

  /**
   * Constructs a new bound SoyToJsSrcCompiler Ant task. This is useful when
   * wrapping the {@link ClosureTemplates} within another task as follows:
   *
   * <p><pre>{@code
   * SoyToJsSrcCompilerTask soyToJsTask = SoyToJsSrcCompilerTask(this);
   * }</pre></p>
   */
  public ClosureTemplates(Task owner) {
    super();
    if (owner != null) {
      bindToOwner(owner);
    }

    this.soyHelperBuilder = new SoyHelper.Builder();

    // Attributes
    this.jsOutputPathFormat = null;
    this.soyPluginClasspath = null;

    // Nested elements
    this.activeDelegatePackageNames = Sets.newHashSet();
    this.javaParseInfo = null;
    this.jsSrcOptions = null;
    this.pluginModules = Lists.newArrayList();
    this.soyFileSets = Lists.newArrayList();
    this.templatesToBeRendered = Lists.newArrayList();
    this.translationOptions = null;
  }


  // Attribute setters

  /**
   * The path to a file containing the mappings for global names to be
   * substituted at compile time. Each line of the file should have the
   * format:
   *
   * <p><pre>{@literal
   * <global_name> = <primitive_data>
   * }</pre></p>
   *
   * <p>where {@code primitive_data} is a valid Soy expression literal for a
   * primitive type (null, boolean, integer, float, or string). Empty lines
   * and lines beginning with {@literal //} are ignored. The file should be
   * encoded in UTF-8. If you need to generate a file in this format from Java,
   * consider using the utility {@code
   * SoyUtils.generateCompileTimeGlobalsFile()}.</p>
   *
   * @param compileTimeGlobalsFile file path to the globals file
   * @throws BuildException if there is an IO error
   */
  public void setCompileTimeGlobalsFile(String compileTimeGlobalsFile) {
    File globalsFile = new File(compileTimeGlobalsFile);

    try {
      this.soyHelperBuilder.compileTimeGlobals(globalsFile);
    } catch (IOException e) {
      throw new BuildException(e);
    }
  }

  /**
   * The scheme to use for handling {@code css} commands. Specifying
   * {@code literal} will cause command text to be inserted as literal text.
   * Specifying {@code reference} will cause command text to be evaluated as
   * data or a global reference. Specifying {@code goog} will cause generation
   * of calls to {@code goog.getCssName}. This option has no effect if the Soy
   * code does not contain {@code css} commands.
   *
   * @param cssHandlingScheme the css handling scheme. Options: {@code goog},
   * {@code literal}, or {@code reference}. Defaults to {@code literal}.
   */
  public void setCssHandlingScheme(String cssHandlingScheme) {
    if ("GOOG".equalsIgnoreCase(cssHandlingScheme.trim())
        || "LITERAL".equalsIgnoreCase(cssHandlingScheme.trim())
        || "REFERENCE".equalsIgnoreCase(cssHandlingScheme.trim())) {

      String schemeUpper = cssHandlingScheme.trim().toUpperCase();

      SoyGeneralOptions.CssHandlingScheme scheme = schemeUpper.equals("GOOG") ?
          SoyGeneralOptions.CssHandlingScheme.BACKEND_SPECIFIC :
          SoyGeneralOptions.CssHandlingScheme.valueOf(schemeUpper);
      this.soyHelperBuilder.cssHandlingScheme(scheme);
    } else {
      throw new BuildException("cssHandlingScheme expected to be one of "
          + "GOOG, LITERAL, or REFERENCE but was \""
          + cssHandlingScheme.trim().toUpperCase() + "\"");
    }
  }

  /**
   * Sets the CSS renaming map file. The JSON object in the file must have
   * keys and values of type string (i.e. no numbers, boolean values,
   * nested arrays, or objects).
   *
   * <p>Any characters outside the outermost matching set of curly braces
   * are ignored. This flexibility means that all of the CSS renaming map
   * output formats supported by <a target="_blank"
   * href="http://code.google.com/p/closure-stylesheets/">Closure Stylesheets
   * </a> (with the exception of Java {@link Properties}) may be passed
   * directly to this method (JSON, CLOSURE_COMPILED, and CLOSURE_UNCOMPILED).
   * See {@link #setCssRenamingMapPropertiesFile(String)}.</p>
   *
   * @param cssRenamingMapFile a CSS renaming map file formatted either as
   *     JSON or {@link Properties}
   * @throws BuildException on error
   */
  public void setCssRenamingMap(String cssRenamingMapFile) {
    try {
      this.soyHelperBuilder.cssRenamingMap(
          CssRenamingMap.createFromJsonFile(new File(cssRenamingMapFile)));
    } catch (IOException e) {
      throw new BuildException(e);
    } catch (JsonParseException jpe) {
      throw new BuildException(jpe);
    }
  }

  /**
   * Sets the CSS renaming map from a Java {@link Properties} file. See {@link
   * #setCssRenamingMap(String)}.
   *
   * @param propertiesFile a Java {@link Properties} file
   * @throws BuildException on error
   */
  public void setCssRenamingMapPropertiesFile(String propertiesFile) {
    try {
      this.soyHelperBuilder.cssRenamingMap(
          CssRenamingMap.createFromJavaPropertiesFile(
              new File(propertiesFile)));
    } catch (IOException e) {
      throw new BuildException(e);
    }
  }

  /**
   * Sets the classpath to use when searching for Soy plugin modules. See
   * <a target="_blank" href="http://ant.apache.org/manual/using.html#path">
   * Path-like Structures</a>.
   *
   * @param classpath a class path
   */
  public void setSoyPluginClasspath(Path classpath) {
    if (this.soyPluginClasspath == null) {
      this.soyPluginClasspath = classpath;
    } else {
      this.soyPluginClasspath.append(classpath);
    }
  }

  /**
   * Creates a {@link org.apache.tools.ant.types.Path} classpath for Soy plugin
   * modules.
   *
   * @return a class path to be configured
   */
  public Path createSoyPluginClasspath() {
    if (this.soyPluginClasspath == null) {
      this.soyPluginClasspath = new Path(getProject());
    }
    return this.soyPluginClasspath.createPath();
  }

  /**
   * Adds a reference to a classpath defined elsewhere to use when searching
   * for Soy plugin modules. See the Ant documentation on references.
   *
   * @param ref a reference to a classpath defined elsewhere (for example,
   *     in a {@literal <classpath>} element)
   */
  public void setSoyPluginClasspathRef(Reference ref) {
    createSoyPluginClasspath().setRefid(ref);
  }


  // Nested element setters

  /**
   * List of active delegate packages. See <a target="_blank"
   * href="https://developers.google.com/closure/templates/docs/commands#deltemplate">
   * deltemplate, delcall, delpackage</a>.
   *
   * @param activeDelegatePackageList list of the active delegate packages
   */
  public void addConfiguredActiveDelegatePackageList(
      ActiveDelegatePackageList activeDelegatePackageList) {
    this.activeDelegatePackageNames.addAll(
        activeDelegatePackageList.getPackages());
  }

  /**
   * Get the set of active delegate package names.
   *
   * @return the set of active delegate package names or an empty Set if no
   *     delegate package names set
   */
  public Set<String> getActiveDelegatePackageNames() {
    return this.activeDelegatePackageNames;
  }

  /**
   * Add a compile-time global variable name-value pair, where the value must
   * be one of the Soy primitive types: null, boolean, integer, float (Java
   * double), or string.
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
   * @param compileTimeGlobal a name-value pair defining a compile-time global
   */
  public void addConfiguredCompileTimeGlobal(NameValuePair compileTimeGlobal) {
    this.soyHelperBuilder.compileTimeGlobal(compileTimeGlobal.getName(),
        compileTimeGlobal.getValue());
  }

  /**
   * Specifies the output Java package and format to use for class names when
   * generating Java classes containing info parsed from template files,
   * including template and parameter names, so that you can avoid the
   * error-prone process of manually typing template and parameter names as
   * strings. There will be one Java class per Soy file.
   *
   * @param javaParseInfo the settings for generating Java parse info classes
   */
  public void addConfiguredJavaParseInfo(JavaParseInfo javaParseInfo) {
    if (this.javaParseInfo == null) {
      this.javaParseInfo = javaParseInfo;
    } else {
      throw new BuildException("nested element javaParseInfo may only "
          + " be used once per <" + getTaskName() + "> task");
    }
  }

  /**
   * Options for compiling Soy templates to JavaScript. See {@link
   * org.closureant.soy.SoyJsSrcOptionsAntType}.
   *
   * @param soyToJsCompileOptions the JavaScript source options
   * @throws BuildException if the outputPathFormat attribute is {@code null}
   */
  public void addConfiguredCompileToJs(
      SoyJsSrcOptionsAntType soyToJsCompileOptions) {
    if (this.jsSrcOptions == null) {
      this.jsSrcOptions = new SoyJsSrcOptions();

      this.jsOutputPathFormat = soyToJsCompileOptions.getOutputPathFormat();
      if (this.jsOutputPathFormat == null) {
        throw new BuildException("<soyToJsCompileOptions> specified but "
            + "the attribute outputPathFormat was not set");
      }

      this.jsSrcOptions.setBidiGlobalDir(soyToJsCompileOptions.getBidiGlobalDir());
      this.jsSrcOptions.setCodeStyle(soyToJsCompileOptions.getCodeStyle());
      this.jsSrcOptions.setGoogMsgsAreExternal(
          soyToJsCompileOptions.googMsgsAreExternal());
      this.jsSrcOptions.setIsUsingIjData(soyToJsCompileOptions.isUsingIjData());
      this.jsSrcOptions.setShouldAllowDeprecatedSyntax(
          soyToJsCompileOptions.shouldAllowDeprecatedSyntax());
      this.jsSrcOptions.setShouldDeclareTopLevelNamespaces(
          soyToJsCompileOptions.shouldDeclareTopLevelNamespaces());
      this.jsSrcOptions.setShouldGenerateGoogMsgDefs(
          soyToJsCompileOptions.shouldGenerateGoogMsgDefs());
      this.jsSrcOptions.setShouldGenerateJsdoc(
          soyToJsCompileOptions.shouldGenerateJsdoc());
      this.jsSrcOptions.setShouldProvideRequireSoyNamespaces(
          soyToJsCompileOptions.shouldProvideRequireSoyNamespaces());
      this.jsSrcOptions.setUseGoogIsRtlForBidiGlobalDir(
          soyToJsCompileOptions.getUseGoogIsRtlForBidiGlobalDir());
    } else {
      throw new BuildException("nested element soyToJsCompileOptions may only "
          + " be used once per <" + getTaskName() + "> task");
    }
  }

  /**
   * Specifies a list of full class names of Guice modules for function and
   * print-directive plugins. The list may be delimited by whitespace and/or
   * commas.
   *
   * @param classNameList list of Guice module class names for function and
   *     print-directive plugins delimited by whitespace and/or commas
   */
  public void addConfiguredPluginModules(ClassNameList classNameList) {
    this.pluginModules.addAll(classNameList.getClassNames());
  }

  /**
   * Adds a set of Soy files. A {@literal <soyfileset>} nested element is an
   * Ant {@link org.apache.tools.ant.types.FileSet}, hence it supports the same
   * parameters as {@link org.apache.tools.ant.types.FileSet}.
   *
   * @param soyFiles a {@link org.apache.tools.ant.types.FileSet} containing
   *     Soy files
   */
  public void addSoyFileSet(FileSet soyFiles) {
    this.soyFileSets.add(soyFiles);
  }

  /**
   * Add a template to render, including its parameter data, injected data,
   * and output options for the rendered content.
   *
   * @param templateRenderOptions the options for rendering a Soy template
   */
  public void addConfiguredRenderTemplate(
      TemplateRenderOptions templateRenderOptions) {
    this.templatesToBeRendered.add(templateRenderOptions);
  }

  /**
   * Soy options for i18n. See {@link org.closureant.soy.TranslationOptions}.
   *
   * @param translationOptions the i18n options
   */
  public void addConfiguredTranslation(
      TranslationOptions translationOptions) {
    if (this.translationOptions == null) {
      this.translationOptions = translationOptions;
    } else {
      throw new BuildException("nested element translationOptions may only be "
          + "used once per <" + getTaskName() + "> task");
    }
  }

  /**
   * Execute the Soy task.
   *
   * @throws org.apache.tools.ant.BuildException on error.
   */
  @Override
  public void execute() {
    List<String> rawSoySources = getAllSoySourcesAbsolutePaths();

    if (rawSoySources.isEmpty()) {
      throw new BuildException("Must specify at least one Soy file.");
    }

    this.soyHelperBuilder.activeDelegatePackageNames(
        this.activeDelegatePackageNames);

    if (this.javaParseInfo != null) {
      this.soyHelperBuilder.javaParseInfo(
          this.javaParseInfo.getOutputJavaPackage(),
          this.javaParseInfo.getSourceOfClassnames());
    }
    if (this.jsSrcOptions != null) {
      this.soyHelperBuilder.soyJsSrcOptions(this.jsSrcOptions);
    }

    for (String filePath : rawSoySources) {
      this.soyHelperBuilder.sourceFile(new File(filePath));
    }


    // Initialize the class loader and any user specified Soy plugin modules.

    Path classpath = new Path(getProject());

    if (this.soyPluginClasspath != null) {
      classpath.append(this.soyPluginClasspath);
    }
    if (this.translationOptions != null) {
      this.soyHelperBuilder.messagePluginModule(
          this.translationOptions.getMessagePluginModule());
      this.soyHelperBuilder.extractedMessageSourceLocale(
          this.translationOptions.getExtractedMessagesSourceLocale());
      this.soyHelperBuilder.extractedMessagesTargetLocales(
          this.translationOptions.getExtractedMessagesTargetLocales());

      if (this.translationOptions.getMessagePluginClasspath() != null) {
        classpath.append(this.translationOptions.getMessagePluginClasspath());
      }
    }

    if (!classpath.toString().isEmpty()) {
      AntClassLoader pluginClassLoader = new AntClassLoader(
          this.getClass().getClassLoader(), getProject(), classpath, true);
      this.soyHelperBuilder.classLoader(pluginClassLoader);
    }
    this.soyHelperBuilder.pluginModules(this.pluginModules);


    // Parse message translation files into Soy message bundles and build the
    // SoyHelper.

    if (this.translationOptions != null) {
      List<String> translationFilePaths =
          this.translationOptions.getTranslationFilePaths(getProject());

      for (String filePath : translationFilePaths) {
        this.soyHelperBuilder.parseTranslationsFromFile(new File(filePath));
      }
    }

    SoyHelper soyHelper = null;
    try {
      soyHelper = soyHelperBuilder.build();
    } catch (IOException e) {
      throw new BuildException(e);
    }

    List<SoySourceFile> soySourceFiles = soyHelper.getListOfSoySourceFiles();


    // Compile Soy templates to JavaScript.

    if (this.jsOutputPathFormat != null) {
      String fileOrFiles = (soySourceFiles.size() > 1) ? "files" : "file";
      log("Compiling " + soySourceFiles.size() + " Soy " + fileOrFiles
          + " to JavaScript...");

      for (SoySourceFile soySourceFile : soySourceFiles) {
        Set<String> outputPathsForSource = Sets.newHashSet();
        Map<String, String> localeToJsSource =
            soySourceFile.compileToJsSrcForEachLocale();

        for (Map.Entry<String, String> entry : localeToJsSource.entrySet()) {
          String locale = entry.getKey();
          String outputPath = replaceOutputPathPlaceholders(
              this.jsOutputPathFormat, soySourceFile, locale);
          if (outputPathsForSource.contains(outputPath)) {
            throw new BuildException("JavaScript output path \"" + outputPath
                + "\" is not unique for locale \"" + locale + "\". Try "
                + "using the output-path placeholder {LOCALE}.");
          }
          outputPathsForSource.add(outputPath);
          File jsFile = new File(outputPath);
          try {
            Files.write(entry.getValue(), jsFile, Charsets.UTF_8);
          } catch (IOException e) {
            throw new BuildException(e);
          }
        }
      }
    }


    // Extract messages for translation.

    if (this.translationOptions != null
        && this.translationOptions.getExtractedMessagesOutputFile() != null) {
      Collection<String> locales =
          this.translationOptions.getExtractedMessagesTargetLocales();
      String localeOrLocales = (locales.size() > 1) ? "locales" : "locale";
      log("Extracting messages for target " + localeOrLocales + ": "
          + Joiner.on(", ").join(locales));

      Set<String> outputPathsForExtractedMsgs = Sets.newHashSet();
      Map<String, String> localeToFileContent =
          soyHelper.extractMessagesForEachTargetLocale();

      for (Map.Entry<String, String> entry : localeToFileContent.entrySet()) {
        String locale = entry.getKey();
        String outputPath = replaceOutputPathPlaceholders(
            this.translationOptions.getExtractedMessagesOutputFile(),
            soySourceFiles.iterator().next(), locale);
        if (outputPathsForExtractedMsgs.contains(outputPath)) {
          throw new BuildException("extracted messages output path \""
              + outputPath + "\" is not unique for locale \"" + locale
              + "\". Try using the output-path placeholder {LOCALE}.");
        }
        outputPathsForExtractedMsgs.add(outputPath);
        File extractedMessagesFile = new File(outputPath);
        try {
          Files.write(entry.getValue(), extractedMessagesFile, Charsets.UTF_8);
        } catch (IOException e) {
          throw new BuildException(e);
        }
      }
    }


    // Generate Java parse info.

    if (this.javaParseInfo != null) {
      if (this.javaParseInfo.getOutputDirectory() == null) {
        throw new BuildException("<javaParseInfo> specified but the "
            + "outputDirectory attribute was not set");
      }
      log("Generating Java parse information files...");

      File outputDir = this.javaParseInfo.getOutputDirectory();

      Map<String, String> fileNameToParseInfo = soyHelper.generateParseInfo();
      for (Map.Entry<String, String> entry : fileNameToParseInfo.entrySet()) {
        File parseInfoFile = new File(outputDir, entry.getKey());
        try {
          Files.write(entry.getValue(), parseInfoFile, Charsets.UTF_8);
        } catch (IOException e) {
          throw new BuildException(e);
        }
      }
    }


    // Render Soy templates.

    if (!this.templatesToBeRendered.isEmpty()) {
      String templateOrTemplates = (this.templatesToBeRendered.size() > 1) ?
          "templates" : "template";
      log("Rendering " + this.templatesToBeRendered.size() + " Soy "
          + templateOrTemplates + "...");

      for (TemplateRenderOptions template : this.templatesToBeRendered) {
        if (template.getOutputPathFormat() == null) {
          throw new BuildException("<rendertemplate> specified for "
              + "template " + template.getFullTemplateName() + " but the "
              + "outputPathFormat attribute was not set");
        }
        Set<String> outputPathsForRenderedContent = Sets.newHashSet();
        Map<String, String> localeToRenderedOutput =
            soyHelper.renderForEachLocale(template.getFullTemplateName(),
                template.getTemplateData(getProject()),
                template.getTemplateInjectedData(getProject()));

        for (Map.Entry<String, String> entry :
            localeToRenderedOutput.entrySet()) {
          String locale = entry.getKey();
          String outputPath = replaceRenderedOutputPathPlaceholders(
              template.getOutputPathFormat(), template, locale);
          if (outputPathsForRenderedContent.contains(outputPath)) {
            throw new BuildException("rendered template output path \""
                + outputPath + "\" is not unique for locale \"" + locale
                + "\". Try using the output-path placeholder {LOCALE}.");
          }
          outputPathsForRenderedContent.add(outputPath);
          try {
            Files.write(entry.getValue(), new File(outputPath), Charsets.UTF_8);
          } catch (IOException e) {
            throw new BuildException(e);
          }
        }
      }
    }
  }

  /**
   * Creates a list of absolute soy source file paths based on the {@literal
   * <soyfiles>} and {@literal <soyfilelist>} nested elements.
   *
   * @return a list of the current soy source file paths
   */
  private List<String> getAllSoySourcesAbsolutePaths() {
    List<String> currentSoySources = Lists.newArrayList();
    
    currentSoySources.addAll(
        AntUtil.getFilePathsFromCollectionOfFileSet(
            getProject(), this.soyFileSets));

    return currentSoySources;
  }

  /**
   * Replaces placeholders (if present) in the output path format and returns
   * the new output path. Supported placeholders: {@code {INPUT_FILE_NAME}},
   * {@code {INPUT_FILE_NAME_NO_EXT}}, {@code {LOCALE}},
   * {@code {LOCALE_LOWER_CASE}}.
   *
   * <p><b>Note:</b> {@code {LOCALE_LOWER_CASE}} turns dash into underscore,
   * e.g. {@code pt-BR} becomes {@code pt_br}.</p>
   *
   * @param outputPathFormat the output path format
   * @param soySourceFile the Soy source file
   * @param locale the locale
   * @return the new output path
   */
  private String replaceOutputPathPlaceholders(String outputPathFormat,
      SoySourceFile soySourceFile, String locale) {
    String newPath = outputPathFormat;

    newPath = newPath.replaceAll("(?i)\\{INPUT_FILE_NAME\\}",
        soySourceFile.getName());
    newPath = newPath.replaceAll("(?i)\\{INPUT_FILE_NAME_NO_EXT\\}",
        soySourceFile.getNameNoExtension());
    newPath = newPath.replaceAll("(?i)\\{LOCALE\\}", locale);

    String localeLower = locale.toLowerCase().replaceAll("-", "_");
    newPath = newPath.replaceAll("(?i)\\{LOCALE_LOWER_CASE\\}", localeLower);
    return newPath;
  }

  /**
   * Replaces placeholders (if present) in the output path format for
   * rendered Soy templates and returns the new output path. Supported
   * placeholders: {@code {TEMPLATE_NAME}}, {@code {FULL_TEMPLATE_NAME}},
   * {@code {LOCALE}}, {@code {LOCALE_LOWER_CASE}}.
   *
   * <p><b>Note:</b> {@code {LOCALE_LOWER_CASE}} turns dash into underscore,
   * e.g. {@code pt-BR} becomes {@code pt_br}.</p>
   *
   * @param outputPathFormat the output path format
   * @param template the Soy source file
   * @param locale the locale
   * @return the new output path
   */
  private String replaceRenderedOutputPathPlaceholders(String outputPathFormat,
      TemplateRenderOptions template, String locale) {
    String newPath = outputPathFormat;

    newPath = newPath.replaceAll("(?i)\\{TEMPLATE_NAME\\}",
        template.getShortTemplateName());
    newPath = newPath.replaceAll("(?i)\\{FULL_TEMPLATE_NAME\\}",
        template.getFullTemplateName());
    newPath = newPath.replaceAll("(?i)\\{LOCALE\\}", locale);

    String localeLower = locale.toLowerCase().replaceAll("-", "_");
    newPath = newPath.replaceAll("(?i)\\{LOCALE_LOWER_CASE\\}", localeLower);
    return newPath;
  }
}
