/*
 * Copyright 2008 Google Inc.
 * Copyright (C) 2012 Christopher Peisert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.closureant.soy;

import com.google.template.soy.jssrc.SoyJsSrcOptions;
import org.apache.tools.ant.BuildException;

/**
 * Ant type wrapper for {@link SoyJsSrcOptions} to be used as a nested element
 * in Ant tasks.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class SoyJsSrcOptionsAntType {

  private String outputPathFormat;
  private SoyJsSrcOptions jsSrcOptions;

  /**
   * Constructs a new Ant type to store JavaScript source options for Soy
   * template compilation.
   */
  public SoyJsSrcOptionsAntType() {
    this.outputPathFormat = null;
    this.jsSrcOptions = new SoyJsSrcOptions();
  }

  /**
   * Sets the bidi global directionality to a static value, 1: ltr, -1: rtl,
   * 0: unspecified. If 0, and {@code useGoogIsRtlForBidiGlobalDir} is
   * {@code false}, the bidi global directionality will actually be inferred
   * from the message bundle locale. This is the recommended mode of operation
   * when {@code shouldGenerateGoogMsgDefs} is false. When {@code
   * shouldGenerateGoogMsgDefs} is {@code true}, the bidi global direction
   * can not be left unspecified, but the recommended way of doing so is via
   * {@code setUseGoogIsRtlForBidiGlobalDir (true)}. Thus, whether {@code
   * shouldGenerateGoogMsgDefs} is {@code true} or not <b>THERE IS USUALLY NO
   * NEED TO USE THIS METHOD!</b>
   *
   * @param bidiGlobalDir 1: ltr, -1: rtl, 0: unspecified. Checks that no
   *     other value is used.
   * @throws BuildException if the specified bidi global direction is not a
   *     valid option or if {@code useGoogIsRtlForBidiGlobalDir} is {@code
   *     true} and {@code bidiGlobalDir} is specified (i.e. -1 or 1)
   *
   */
  public void setBidiGlobalDir(int bidiGlobalDir) {
    try {
      this.jsSrcOptions.setBidiGlobalDir(bidiGlobalDir);
    } catch (IllegalStateException e) {
      throw new BuildException(e.getMessage());
    }
  }

  /**
   * Returns the static bidi global directionality, 1: ltr, -1: rtl,
   * 0: unspecified.
   */
  public int getBidiGlobalDir() {
    return this.jsSrcOptions.getBidiGlobalDir();
  }

  /**
   * Sets the output variable code style to use.
   *
   * @param codeStyle The code style to set. Options: CONCAT or STRINGBUILDER
   * @throws BuildException if {@code codeStyle} is not a valid option
   */
  public void setCodeStyle(String codeStyle) {
    if (SoyJsSrcOptions.CodeStyle.CONCAT.toString()
        .equalsIgnoreCase(codeStyle)) {
      this.jsSrcOptions.setCodeStyle(SoyJsSrcOptions.CodeStyle.CONCAT);
    } else if (SoyJsSrcOptions.CodeStyle.STRINGBUILDER.toString()
        .equalsIgnoreCase(codeStyle)) {
      this.jsSrcOptions.setCodeStyle(SoyJsSrcOptions.CodeStyle.STRINGBUILDER);
    } else {
      throw new BuildException("codeStyle expected to be CONCAT or "
          + "STRINGBUILDER but was \""
          + codeStyle.trim().toUpperCase() + "\"");
    }
  }

  /** Returns the currently set code style. */
  public SoyJsSrcOptions.CodeStyle getCodeStyle() {
    return this.jsSrcOptions.getCodeStyle();
  }

  /**
   * Sets whether the generated Closure Library message definitions are for
   * external messages (only applicable if {@code shouldGenerateGoogMsgDefs}
   * is {@code true}).
   *
   * <p>If this option is true, then we generate:</p>
   *
   * <p><pre>{@literal
   *   var MSG_EXTERNAL_<soyGeneratedMsgId> = goog.getMsg(...);
   * }</pre></p>
   *
   * <p>If this option is false, then we generate:</p>
   *
   * <p><pre>{@literal
   *   var MSG_UNNAMED_<uniquefier> = goog.getMsg(...);
   * }</pre></p>
   *
   * @param googMsgsAreExternal The value to set.
   */
  public void setGoogMsgsAreExternal(boolean googMsgsAreExternal) {
    this.jsSrcOptions.setGoogMsgsAreExternal(googMsgsAreExternal);
  }

  /**
   * Returns whether the generated Closure Library message definitions are for
   * external messages (only applicable if {@code shouldGenerateGoogMsgDefs}
   * is {@code true}).
   *
   * <p>See {@link #setGoogMsgsAreExternal(boolean)}.</p>
   */
  public boolean googMsgsAreExternal() {
    return this.jsSrcOptions.googMsgsAreExternal();
  }

  /**
   * Sets whether to enable use of injected data (syntax is '$ij.*').
   * @param isUsingIjData The code style to set.
   */
  public void setIsUsingIjData(boolean isUsingIjData) {
    this.jsSrcOptions.setIsUsingIjData(isUsingIjData);
  }

  /** Returns whether use of injected data is currently enabled. */
  public boolean isUsingIjData() {
    return this.jsSrcOptions.isUsingIjData();
  }

  /**
   * A format string that specifies how to build the path to each output file.
   * If not generating localized JS, then there will be one output JS file
   * (UTF-8) for each input Soy file. If generating localized JS, then there
   * will be one output JS file for each combination of input Soy file and
   * locale. The format string can include literal characters as well as the
   * placeholders {@code {INPUT_FILE_NAME}}, {@code {INPUT_FILE_NAME_NO_EXT}},
   * {@code {LOCALE}}, {@code {LOCALE_LOWER_CASE}}.
   *
   * <p><b>Note:</b> {@code {LOCALE_LOWER_CASE}} turns dash into
   * underscore, e.g. {@code pt-BR} becomes {@code pt_br}.</p>
   *
   * @param outputPathFormat format string specifying how to build the path
   *     to each output file
   */
  public void setOutputPathFormat(String outputPathFormat) {
    this.outputPathFormat = outputPathFormat;
  }

  /**
   * Returns the format for generated JavaScript output files. See {@link
   * #setOutputPathFormat(String)}.
   */
  public String getOutputPathFormat() {
    return this.outputPathFormat;
  }

  /**
   * Sets whether to allow deprecated syntax (semi backwards compatible mode).
   * @param shouldAllowDeprecatedSyntax The value to set.
   */
  public void setShouldAllowDeprecatedSyntax(
      boolean shouldAllowDeprecatedSyntax) {
    this.jsSrcOptions.setShouldAllowDeprecatedSyntax(
        shouldAllowDeprecatedSyntax);
  }

  /**
   * Returns whether we're set to allow deprecated syntax (semi backwards
   * compatible mode).
   */
  public boolean shouldAllowDeprecatedSyntax() {
    return this.jsSrcOptions.shouldAllowDeprecatedSyntax();
  }

  /**
   * OPTION NOT CURRENTLY SUPPORTED IN ANT TASKS
   *
   * Sets whether we should generate code to provide/require template JS
   * functions.
   *
   * @param shouldProvideRequireJsFunctions The value to set.
   * @throws IllegalStateException if {@code shouldDeclareTopLevelNamespaces}
   *     is {@code false} and {@code shouldProvideRequireSoyNamespaces} is
   *     {@code true}
   */
  /*public void setShouldProvideRequireJsFunctions(
      boolean shouldProvideRequireJsFunctions) {
    this.jsSrcOptions.setShouldProvideRequireJsFunctions(
        shouldProvideRequireJsFunctions);
  }*/

  /**
   * OPTION NOT CURRENTLY SUPPORTED IN ANT TASKS
   *
   * Returns whether we're set to generate code to provide/require template JS
   * functions.
   */
  /*public boolean shouldProvideRequireJsFunctions() {
    return this.jsSrcOptions.shouldProvideRequireJsFunctions();
  }*/

  /**
   * OPTION NOT CURRENTLY SUPPORTED IN ANT TASKS
   *
   * Sets whether we should generate code to provide both Soy namespaces and
   * JS functions.
   *
   * @param shouldProvideBothSoyNamespacesAndJsFunctions The value to set.
   * @throws IllegalStateException if one of {@code
   *     shouldProvideRequireSoyNamespaces} or {@code
   *     shouldProvideRequireJsFunctions} is not set to {@code true}
   */
  /*public void setShouldProvideBothSoyNamespacesAndJsFunctions(
      boolean shouldProvideBothSoyNamespacesAndJsFunctions) {
    this.jsSrcOptions.setShouldProvideBothSoyNamespacesAndJsFunctions(
        shouldProvideBothSoyNamespacesAndJsFunctions);
  }*/

  /**
   * OPTION NOT CURRENTLY SUPPORTED IN ANT TASKS
   *
   * Returns whether we should generate code to provide both Soy namespaces
   * and JS functions.
   */
  /*public boolean shouldProvideBothSoyNamespacesAndJsFunctions() {
    return this.jsSrcOptions.shouldProvideBothSoyNamespacesAndJsFunctions();
  }*/

  /**
   * Sets whether we should generate code to declare the top level namespace.
   * @param shouldDeclareTopLevelNamespaces The value to set.
   * @throws BuildException if {@code shouldDeclareTopLevelNamespaces}
   *     is {@code false} and {@code shouldProvideRequireSoyNamespaces} is
   *     {@code true}
   */
  public void setShouldDeclareTopLevelNamespaces(
      boolean shouldDeclareTopLevelNamespaces) {
    try {
      this.jsSrcOptions.setShouldDeclareTopLevelNamespaces(
          shouldDeclareTopLevelNamespaces);
    } catch (IllegalStateException e) {
      throw new BuildException(e.getMessage());
    }
  }

  /** Returns whether we should attempt to declare the top level namespace. */
  public boolean shouldDeclareTopLevelNamespaces() {
    return this.jsSrcOptions.shouldDeclareTopLevelNamespaces();
  }

  /**
   * Sets whether we should generate Closure Library message definitions (i.e.
   * goog.getMsg).
   *
   * @param shouldGenerateGoogMsgDefs The value to set.
   */
  public void setShouldGenerateGoogMsgDefs(boolean shouldGenerateGoogMsgDefs) {
    this.jsSrcOptions.setShouldGenerateGoogMsgDefs(shouldGenerateGoogMsgDefs);
  }

  /**
   * Returns whether we should generate Closure Library message definitions
   * (i.e. goog.getMsg).
   */
  public boolean shouldGenerateGoogMsgDefs() {
    return this.jsSrcOptions.shouldGenerateGoogMsgDefs();
  }

  /**
   * Sets whether we should generate JSDoc with type info for the Closure
   * Compiler.
   *
   * @param shouldGenerateJsdoc The value to set.
   */
  public void setShouldGenerateJsdoc(boolean shouldGenerateJsdoc) {
    this.jsSrcOptions.setShouldGenerateJsdoc(shouldGenerateJsdoc);
  }

  /**
   * Returns whether we should generate JSDoc with type info for the Closure
   * Compiler.
   */
  public boolean shouldGenerateJsdoc() {
    return this.jsSrcOptions.shouldGenerateJsdoc();
  }

  /**
   * Sets whether we should generate code to provide/require Soy namespaces.
   * @param shouldProvideRequireSoyNamespaces The value to set.
   * @throws BuildException if {@code shouldDeclareTopLevelNamespaces}
   *     is {@code false} and {@code shouldProvideRequireSoyNamespaces} is
   *     {@code true}
   */
  public void setShouldProvideRequireSoyNamespaces(
      boolean shouldProvideRequireSoyNamespaces) {
    try {
      this.jsSrcOptions.setShouldProvideRequireSoyNamespaces(
          shouldProvideRequireSoyNamespaces);
    } catch (IllegalStateException e) {
      throw new BuildException(e.getMessage());
    }
  }

  /**
   * Returns whether we're set to generate code to provide/require Soy
   * namespaces.
   */
  public boolean shouldProvideRequireSoyNamespaces() {
    return this.jsSrcOptions.shouldProvideRequireSoyNamespaces();
  }

  /**
   * Sets the Javascript code snippet that will evaluate at template runtime
   * to a boolean value indicating whether the bidi global direction is rtl.
   * Can only be used when {@code shouldGenerateGoogMsgDefs} is {@code true}.
   *
   * @param useGoogIsRtlForBidiGlobalDir Whether to determine the bidi global
   *     direction at template runtime by evaluating goog.i18n.bidi.IS_RTL.
   * @throws BuildException if 1) {@code useGoogIsRtlForBidiGlobalDir} is
   *     {@code true} and {@code shouldGenerateGoogMsgDefs} is {@code false};
   *     2) {@code useGoogIsRtlForBidiGlobalDir} is {@code true} and {@code
   *     shouldProvideRequireSoyNamepsaces} is {@code false}; 3) {@code
   *     useGoogIsRtlForBidiGlobalDir} is {@code true} and {@code bidiGlobalDir}
   *     is specified (i.e. -1 or 1)
   */
  public void setUseGoogIsRtlForBidiGlobalDir(
      boolean useGoogIsRtlForBidiGlobalDir) {
    try {
      this.jsSrcOptions.setUseGoogIsRtlForBidiGlobalDir(
          useGoogIsRtlForBidiGlobalDir);
    } catch (IllegalStateException e) {
      throw new BuildException(e.getMessage());
    }
  }

  /**
   * Returns whether to determine the bidi global direction at template runtime
   * by evaluating {@code goog.i18n.bidi.IS_RTL}. May only be {@code true}
   * when {@code shouldGenerateGoogMsgDefs} is {@code true}.
   */
  public boolean getUseGoogIsRtlForBidiGlobalDir() {
    return this.jsSrcOptions.getUseGoogIsRtlForBidiGlobalDir();
  }
}