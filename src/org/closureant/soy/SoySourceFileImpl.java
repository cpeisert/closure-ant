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
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.jssrc.SoyJsSrcOptions;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.shared.SoyGeneralOptions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import org.closureant.base.JsClosureSourceFile;
import org.closureant.base.SourceFileFactory;

/**
 * Default implementation of {@link SoySourceFile}. To construct new instances
 * of {@link SoySourceFile}, pass Soy {@link java.io.File}s to {@link
 * SoyHelper.Builder} and then call {@link
 * SoyHelper#getListOfSoySourceFiles()}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
class SoySourceFileImpl implements SoySourceFile {

  // Cached results lazily initialized using the double-check idiom.
  // See Effective Java, 2nd Edition, Item 71: Use lazy initialization
  // judiciously.
  private volatile ImmutableMap<String, String> generatedParseInfo;
  private volatile String jsSourceCode;
  private volatile ImmutableMap<String, String> localeToJsSourceCode;

  private final String filePath;
  private final SoyJsSrcOptions jsSrcOptions;
  private final Set<SoyMsgBundle> msgBundleSet;
  private final String parseInfoJavaClassname;
  private final String parseInfoJavaPackage;
  private final Set<String> provides;
  private final Set<String> requires;
  private final SoyFileSet soyFileSet;
  private final String soyNamespace;
  private final String soySourceCode;

  /**
   * Constructs a {@link SoySourceFile} for the given file.
   *
   * @param soyFile The Soy file.
   * @param generalOptions General options including: {@code
   *     allowExternalCalls, compileTimeGlobals, and cssHandlingScheme}.
   * @param jsSrcOptions The options for compiling this Soy file to JavaScript.
   * @param msgBundleSet The set of message bundles, each of which contains
   *     a complete set of messages for some language/locale.
   * @throws IOException If there is an error reading the Soy file.
   * @throws NullPointerException If any of the parameters are {@code null}.
   */
  SoySourceFileImpl(File soyFile, SoyGeneralOptions generalOptions,
      SoyJsSrcOptions jsSrcOptions, Set<SoyMsgBundle> msgBundleSet)
      throws IOException {
    this(Files.toString(soyFile, Charsets.UTF_8), soyFile.getCanonicalPath(),
        generalOptions, jsSrcOptions, msgBundleSet, null, null);
  }

  /**
   * Constructs a {@link SoySourceFile} for the given file.
   *
   * @param soyFile The Soy file.
   * @param generalOptions General options including: {@code
   *     allowExternalCalls, compileTimeGlobals, and cssHandlingScheme}.
   * @param jsSrcOptions The options for compiling this Soy file to JavaScript.
   * @param msgBundleSet The set of message bundles, each of which contains
   *     a complete set of messages for some language/locale.
   * @param parseInfoJavaPackage The Java package for the generated classes.
   *     May be {@code null}.
   * @param parseInfoJavaClassname Source of the generated Java class names.
   *     Must be one of "filename", "namespace", or "generic". May be
   *     {@code null}.
   * @throws IOException If there is an error reading the Soy file.
   * @throws NullPointerException If any of the parameters are {@code null}.
   */
  SoySourceFileImpl(File soyFile, SoyGeneralOptions generalOptions,
      SoyJsSrcOptions jsSrcOptions, Set<SoyMsgBundle> msgBundleSet,
      String parseInfoJavaClassname, String parseInfoJavaPackage)
      throws IOException {
    this(Files.toString(soyFile, Charsets.UTF_8), soyFile.getCanonicalPath(),
        generalOptions, jsSrcOptions, msgBundleSet, parseInfoJavaClassname,
        parseInfoJavaPackage);
  }

  /**
   * Constructs a {@link SoySourceFile} for a resource URL.
   *
   * @param soyFileURL URL for the Soy file.
   * @param generalOptions General options including: {@code
   *     allowExternalCalls, compileTimeGlobals, and cssHandlingScheme}.
   * @param jsSrcOptions The options for compiling this Soy file to JavaScript.
   * @param msgBundleSet The set of message bundles, each of which contains
   *     a complete set of messages for some language/locale.
   * @throws IOException If there is an error reading the Soy file.
   * @throws NullPointerException If any of the parameters are {@code null}.
   */
  SoySourceFileImpl(URL soyFileURL, SoyGeneralOptions generalOptions,
                    SoyJsSrcOptions jsSrcOptions, Set<SoyMsgBundle> msgBundleSet)
      throws IOException {
    this(Resources.toString(soyFileURL, Charsets.UTF_8), soyFileURL.getPath(),
        generalOptions, jsSrcOptions, msgBundleSet, null, null);
  }

  /**
   * Constructs a {@link SoySourceFile} for a resource URL.
   *
   * @param soyFileURL URL for the Soy file.
   * @param generalOptions General options including: {@code
   *     allowExternalCalls, compileTimeGlobals, and cssHandlingScheme}.
   * @param jsSrcOptions The options for compiling this Soy file to JavaScript.
   * @param msgBundleSet The set of message bundles, each of which contains
   *     a complete set of messages for some language/locale.
   * @param parseInfoJavaPackage The Java package for the generated classes.
   *     May be {@code null}.
   * @param parseInfoJavaClassname Source of the generated Java class names.
   *     Must be one of "filename", "namespace", or "generic". May be
   *     {@code null}.
   * @throws IOException If there is an error reading the Soy file.
   * @throws NullPointerException If any of the parameters are {@code null}.
   */
  SoySourceFileImpl(URL soyFileURL, SoyGeneralOptions generalOptions,
      SoyJsSrcOptions jsSrcOptions, Set<SoyMsgBundle> msgBundleSet,
      String parseInfoJavaClassname, String parseInfoJavaPackage)
      throws IOException {
    this(Resources.toString(soyFileURL, Charsets.UTF_8), soyFileURL.getPath(),
        generalOptions, jsSrcOptions, msgBundleSet, parseInfoJavaClassname,
        parseInfoJavaPackage);
  }

  /**
   * Constructs a {@link SoySourceFile} for the given file.
   *
   * @param content The Soy file content.
   * @param filePath The path to the Soy file (used for messages only).
   * @param generalOptions General options including: {@code
   *     allowExternalCalls, compileTimeGlobals, and cssHandlingScheme}.
   * @param jsSrcOptions Options for compiling this Soy file to JavaScript.
   * @param msgBundleSet The set of message bundles, each of which contains
   *     a complete set of messages for some language/locale.
   * @param parseInfoJavaPackage The Java package for the generated classes.
   *     May be {@code null}.
   * @param parseInfoJavaClassname Source of the generated Java class names.
   *     Must be one of "filename", "namespace", or "generic". May be
   *     {@code null}.
   * @throws IOException If there is an error reading the Soy file.
   * @throws NullPointerException If {@code soyFile}, {@code generalOptions},
   *     {@code jsSrcOption}, or {@code msgBundleSet} is {@code null}.
   */
  SoySourceFileImpl(CharSequence content, String filePath,
      SoyGeneralOptions generalOptions,
      SoyJsSrcOptions jsSrcOptions, Set<SoyMsgBundle> msgBundleSet,
      String parseInfoJavaClassname, String parseInfoJavaPackage)
      throws IOException {

    Preconditions.checkNotNull(content, "content was null");
    Preconditions.checkNotNull(generalOptions, "generalOptions was null");
    Preconditions.checkNotNull(jsSrcOptions, "jsSrcOption was null");
    Preconditions.checkNotNull(msgBundleSet, "msgBundleSet was null");

    this.filePath = filePath;
    this.soySourceCode = content.toString();
    this.generatedParseInfo = null;
    this.jsSourceCode = null;
    this.jsSrcOptions = jsSrcOptions;
    this.msgBundleSet = msgBundleSet;
    this.parseInfoJavaClassname = parseInfoJavaClassname;
    this.parseInfoJavaPackage = parseInfoJavaPackage;

    this.soyFileSet = new SoyFileSet.Builder()
        .add(this.soySourceCode, this.filePath)
        .setAllowExternalCalls(generalOptions.allowExternalCalls())
        .setCompileTimeGlobals(generalOptions.getCompileTimeGlobals())
        .setCssHandlingScheme(generalOptions.getCssHandlingScheme())
        .build();

    // Generate JS code with goog.provide/goog.require to initialize
    // dependency information.

    SoyJsSrcOptions options = new SoyJsSrcOptions();
    options.setCodeStyle(SoyJsSrcOptions.CodeStyle.CONCAT);
    options.setShouldProvideRequireSoyNamespaces(true);

    SoyFileSet tempFileSet = new SoyFileSet.Builder()
        .add(this.soySourceCode, this.filePath)
        .setAllowExternalCalls(true)
        .build();

    String jsCode = tempFileSet.compileToJsSrc(options, null).get(0);

    JsClosureSourceFile jsSourceFile =
        SourceFileFactory.newJsClosureSourceFile(this.filePath, jsCode);
    this.provides = Sets.newHashSet(jsSourceFile.getProvides());
    this.requires = Sets.newHashSet(jsSourceFile.getRequires());
    this.soyNamespace = this.provides.iterator().next();
  }

  public String compileToJsSrc() {
    // "Double-check" idiom for lazy initialization
    String jsSrc = this.jsSourceCode;
    if (jsSrc == null) {
      synchronized (this) {
        jsSrc = this.jsSourceCode;
        if (jsSrc == null) {
          this.jsSourceCode = jsSrc = this.soyFileSet.compileToJsSrc(
              this.jsSrcOptions, /* SoyMsgBundle */ null).get(0);
        }
      }
    }
    return jsSrc;
  }

  public ImmutableMap<String, String> compileToJsSrcForEachLocale() {
    // "Double-check" idiom for lazy initialization
    ImmutableMap<String, String> localeToJs = this.localeToJsSourceCode;
    if (localeToJs == null) {
      synchronized (this) {
        localeToJs = this.localeToJsSourceCode;
        if (localeToJs == null) {
          ImmutableMap.Builder<String, String> mapBuilder =
              ImmutableMap.builder();

          for (SoyMsgBundle msgBundle : this.msgBundleSet) {
            String locale = msgBundle.getLocaleString();
            String jsCode = this.soyFileSet.compileToJsSrc(
                this.jsSrcOptions, msgBundle).get(0);
            mapBuilder.put(locale, jsCode);
          }
          if (this.msgBundleSet.isEmpty()) {
            if (this.jsSourceCode == null) {
              this.jsSourceCode = this.soyFileSet.compileToJsSrc(
                  this.jsSrcOptions, /* SoyMsgBundle */ null).get(0);
            }
            mapBuilder.put("en", this.jsSourceCode);
          }

          this.localeToJsSourceCode = localeToJs = mapBuilder.build();
        }
      }
    }
    return localeToJs;
  }

  public ImmutableMap<String, String> generateParseInfo()
      throws SoySyntaxException {
    // "Double-check" idiom for lazy initialization
    ImmutableMap<String, String> fileNameToParseInfo = this.generatedParseInfo;
    if (fileNameToParseInfo == null) {
      synchronized (this) {
        fileNameToParseInfo = this.generatedParseInfo;
        if (fileNameToParseInfo == null) {
          this.generatedParseInfo = fileNameToParseInfo =
              this.soyFileSet.generateParseInfo(
                  this.parseInfoJavaPackage, this.parseInfoJavaClassname);
        }
      }
    }
    return fileNameToParseInfo;
  }

  public String getAbsolutePath() {
    return this.filePath;
  }

  public String getCode() {
    return this.soySourceCode;
  }

  public String getDirectory() {
    return getParentDirectory(this.filePath);
  }

  public String getName() {
    return getFileNameFromAbsolutePath(this.filePath);
  }

  public String getNameNoExtension() {
    String fileName = getFileNameFromAbsolutePath(this.filePath);
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex == -1) {
      lastDotIndex = fileName.length();
    }
    return fileName.substring(0, lastDotIndex);
  }

  public String getNamespace() {
    return this.soyNamespace;
  }

  public Collection<String> getProvides() {
    return ImmutableSet.copyOf(this.provides);
  }

  public Collection<String> getRequires() {
    return ImmutableSet.copyOf(this.requires);
  }

  /**
   * Returns the file name.
   *
   * @return the string form of this JavaScript source file
   */
  @Override public String toString() {
    return getFileNameFromAbsolutePath(this.filePath);
  }

  /**
   * Gets the file name including the extension from the specified file path.
   * If a file separator (that is, a forward slash or backslash) is not found,
   * the specified path is returned.
   *
   * @param absolutePath The absolute path.
   * @return The file name including the extension.
   */
  private String getFileNameFromAbsolutePath(String absolutePath) {
    int index = getLastIndexOfFileSeparator(absolutePath);
    if (index == -1) {
      return absolutePath;
    }
    return absolutePath.substring(index + 1);
  }

  /**
   * Gets the parent directory of the specified file path. If a file
   * separator (that is, a forward slash or backslash) is not found, the empty
   * string is returned.
   *
   * @param absolutePath The absolute path.
   * @return The parent directory of the specified file.
   */
  private String getParentDirectory(String absolutePath) {
    int index = getLastIndexOfFileSeparator(absolutePath);
    if (index == -1) {
      return "";
    }
    return absolutePath.substring(0, index);
  }

  private int getLastIndexOfFileSeparator(String path) {
    int index = path.lastIndexOf('/');
    return (index != -1) ? index : path.lastIndexOf('\\');
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SoySourceFileImpl that = (SoySourceFileImpl) o;

    if (filePath != null ? !filePath.equals(that.filePath) : that.filePath !=
        null) {
      return false;
    }
    if (generatedParseInfo != null ? !generatedParseInfo.equals(that
        .generatedParseInfo) : that.generatedParseInfo != null) {
      return false;
    }
    if (jsSrcOptions != null ? !jsSrcOptions.equals(that.jsSrcOptions) : that
        .jsSrcOptions != null) {
      return false;
    }
    if (localeToJsSourceCode != null ? !localeToJsSourceCode.equals(that
        .localeToJsSourceCode) : that.localeToJsSourceCode != null) {
      return false;
    }
    if (msgBundleSet != null ? !msgBundleSet.equals(that.msgBundleSet) : that
        .msgBundleSet != null) {
      return false;
    }
    if (parseInfoJavaClassname != null ? !parseInfoJavaClassname.equals(that
        .parseInfoJavaClassname) : that.parseInfoJavaClassname != null) {
      return false;
    }
    if (parseInfoJavaPackage != null ? !parseInfoJavaPackage.equals(that
        .parseInfoJavaPackage) : that.parseInfoJavaPackage != null) {
      return false;
    }
    if (soySourceCode != null ? !soySourceCode.equals(that.soySourceCode) :
        that.soySourceCode != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = generatedParseInfo != null ? generatedParseInfo.hashCode() : 0;
    result = 31 * result + (localeToJsSourceCode != null ?
        localeToJsSourceCode.hashCode() : 0);
    result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
    result = 31 * result + (jsSrcOptions != null ? jsSrcOptions.hashCode() : 0);
    result = 31 * result + (msgBundleSet != null ? msgBundleSet.hashCode() : 0);
    result = 31 * result + (parseInfoJavaClassname != null ?
        parseInfoJavaClassname.hashCode() : 0);
    result = 31 * result + (parseInfoJavaPackage != null ? parseInfoJavaPackage.hashCode() : 0);
    result = 31 * result + (soySourceCode != null ? soySourceCode.hashCode() : 0);
    return result;
  }
}
