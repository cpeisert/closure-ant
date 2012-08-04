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
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.jssrc.SoyJsSrcOptions;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.shared.SoyGeneralOptions;

import java.io.File;
import java.io.IOException;
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

  private final SoyJsSrcOptions jsSrcOptions;
  private final Set<SoyMsgBundle> msgBundleSet;
  private final String parseInfoJavaClassname;
  private final String parseInfoJavaPackage;
  private final Set<String> provides;
  private final Set<String> requires;
  private final File soyFile;
  private final String soyFileName;
  private final SoyFileSet soyFileSet;
  private final String soyNamespace;
  private final String soySourceCode;

  /**
   * Constructs a {@link SoySourceFile} for the given file.
   *
   * @param soyFile the physical Soy file
   * @param generalOptions general options including: {@code
   *     allowExternalCalls, compileTimeGlobals, and cssHandlingScheme}
   * @param jsSrcOptions the options for compiling this Soy file to JavaScript
   * @param msgBundleSet the set of message bundles, each of which contains
   *     a complete set of messages for some language/locale
   * @throws IOException if there is an error reading the Soy file
   * @throws NullPointerException if any of the parameters are {@code null}
   */
  SoySourceFileImpl(File soyFile, SoyGeneralOptions generalOptions,
      SoyJsSrcOptions jsSrcOptions, Set<SoyMsgBundle> msgBundleSet)
      throws IOException {
    this(soyFile, generalOptions, jsSrcOptions, msgBundleSet, null, null);
  }

  /**
   * Constructs a {@link SoySourceFile} for the given file.
   *
   * @param soyFile the physical Soy file
   * @param generalOptions general options including: {@code
   *     allowExternalCalls, compileTimeGlobals, and cssHandlingScheme}
   * @param jsSrcOptions the options for compiling this Soy file to JavaScript
   * @param msgBundleSet the set of message bundles, each of which contains
   *     a complete set of messages for some language/locale
   * @param parseInfoJavaPackage The Java package for the generated classes.
   *     May be {@code null}.
   * @param parseInfoJavaClassname Source of the generated Java class names.
   *     Must be one of "filename", "namespace", or "generic". May be
   *     {@code null}.
   * @throws IOException if there is an error reading the Soy file
   * @throws NullPointerException if {@code soyFile}, {@code generalOptions},
   *     {@code jsSrcOption}, or {@code msgBundleSet} is {@code null}
   */
  SoySourceFileImpl(File soyFile, SoyGeneralOptions generalOptions,
      SoyJsSrcOptions jsSrcOptions, Set<SoyMsgBundle> msgBundleSet,
      String parseInfoJavaClassname, String parseInfoJavaPackage)
      throws IOException {

    Preconditions.checkNotNull(soyFile, "soyFile was null");
    Preconditions.checkNotNull(generalOptions, "generalOptions was null");
    Preconditions.checkNotNull(jsSrcOptions, "jsSrcOption was null");
    Preconditions.checkNotNull(msgBundleSet, "msgBundleSet was null");

    this.generatedParseInfo = null;
    this.jsSourceCode = null;
    this.jsSrcOptions = jsSrcOptions;
    this.msgBundleSet = msgBundleSet;
    this.parseInfoJavaClassname = parseInfoJavaClassname;
    this.parseInfoJavaPackage = parseInfoJavaPackage;

    this.soyFileSet = new SoyFileSet.Builder()
        .add(soyFile)
        .setAllowExternalCalls(generalOptions.allowExternalCalls())
        .setCompileTimeGlobals(generalOptions.getCompileTimeGlobals())
        .setCssHandlingScheme(generalOptions.getCssHandlingScheme())
        .build();

    // Generate JS code with goog.provide/goog.require to initialize
    // dependency information.

    SoyJsSrcOptions options = new SoyJsSrcOptions();
    options.setCodeStyle(SoyJsSrcOptions.CodeStyle.CONCAT);
    options.setShouldProvideRequireSoyNamespaces(true);

    SoyFileSet tempFileSet = new SoyFileSet.Builder().add(soyFile)
        .setAllowExternalCalls(true).build();

    String jsCode = tempFileSet.compileToJsSrc(options, null).get(0);

    JsClosureSourceFile jsSourceFile =
        SourceFileFactory.newJsClosureSourceFile(soyFile.getName(), jsCode);
    this.provides = Sets.newHashSet(jsSourceFile.getProvides());
    this.requires = Sets.newHashSet(jsSourceFile.getRequires());
    this.soyFile = soyFile;
    this.soyFileName = soyFile.getName();
    this.soyNamespace = this.provides.iterator().next();
    this.soySourceCode = Files.toString(soyFile, Charsets.UTF_8);
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
    try {
      return this.soyFile.getCanonicalPath();
    } catch (IOException e) {
      return this.soyFile.getAbsolutePath();
    }
  }

  public String getCode() {
    return this.soySourceCode;
  }

  public String getDirectory() {
    return this.soyFile.getParent();
  }

  public String getName() {
    return this.soyFileName;
  }

  public String getNameNoExtension() {
    int lastDotIndex = this.soyFileName.lastIndexOf('.');
    if (lastDotIndex == -1) {
      lastDotIndex = this.soyFileName.length();
    }
    return this.soyFileName.substring(0, lastDotIndex);
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

  public String getRelativePath() {
    return this.soyFile.getPath();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SoySourceFileImpl)) {
      return false;
    }

    SoySourceFileImpl that = (SoySourceFileImpl) o;

    if (!jsSrcOptions.equals(that.jsSrcOptions)) {
      return false;
    }
    if (!msgBundleSet.equals(that.msgBundleSet)) {
      return false;
    }
    if (parseInfoJavaClassname != null ? !parseInfoJavaClassname.equals
        (that.parseInfoJavaClassname) : that.parseInfoJavaClassname != null) {
      return false;
    }
    if (parseInfoJavaPackage != null ? !parseInfoJavaPackage.equals(that
        .parseInfoJavaPackage) : that.parseInfoJavaPackage != null) {
      return false;
    }
    if (!soyFile.equals(that.soyFile)) {
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
    int result = jsSrcOptions.hashCode();
    result = 31 * result + msgBundleSet.hashCode();
    result = 31 * result + (parseInfoJavaClassname != null ?
        parseInfoJavaClassname.hashCode() : 0);
    result = 31 * result + (parseInfoJavaPackage != null ? parseInfoJavaPackage.hashCode() : 0);
    result = 31 * result + soyFile.hashCode();
    result = 31 * result + (soySourceCode != null ? soySourceCode.hashCode() : 0);
    return result;
  }

  /**
   * Returns the file name.
   *
   * @return the string form of this JavaScript source file
   */
  @Override public String toString() {
    return this.soyFileName;
  }
}
