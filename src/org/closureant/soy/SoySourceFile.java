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

import com.google.common.collect.ImmutableMap;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.base.SoySyntaxException;

import java.util.Collection;
import java.io.File;

import org.closureant.base.ProvidesRequiresSourceFile;

/**
 * A Soy (Closure Templates) source file. To construct new instances of {@link
 * SoySourceFile}, pass Soy {@link File}s to {@link SoyHelper.Builder} and
 * then call {@link
 * SoyHelper#getListOfSoySourceFiles()}.
 *
 * <p><b>Note:</b> Rendering is per template not per file, since templates may
 * have dependencies across multiple Soy files. See the {@code render} methods
 * in {@link SoyHelper}.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public interface SoySourceFile extends ProvidesRequiresSourceFile {

  /**
   * Compiles the Soy source code into JavaScript using the settings from the
   * {@link  SoyHelper} using the messages in the Soy file (i.e. ignoring any
   * {@link SoyMsgBundle} added to the {@link SoyHelper}.
   */
  String compileToJsSrc();

  /**
   * Compiles the Soy source code into JavaScript for each {@link SoyMsgBundle}
   * that has been added to the {@link SoyHelper} where each localized
   * JavaScript compilation is returned as a separate map entry {@literal
   * <key=locale, value=JavaScript>}. If no Soy message bundles are set,
   * then a map is returned containing one entry with key equal to "en" and
   * value equal to the JavaScript returned by {@link #compileToJsSrc()}.
   *
   * @return a map from locale string (returned by {@link
   *     com.google.template.soy.msgs.SoyMsgBundle#getLocaleString()}) to the
   *     compiled JavaScript code for that locale
   */
  ImmutableMap<String, String> compileToJsSrcForEachLocale();

  /**
   * Generates a Java class containing info parsed from this template files so
   * that you can avoid the error-prone process of manually typing template
   * and parameter names as strings.
   *
   * <p><b>Note:</b> the Java package for the generated classes as well as
   * the source to use for generating the class names must be set using
   * {@link SoyHelper.Builder#javaParseInfo(String, String)}</p>
   *
   * @return A map from generated file name (of the form "<*>SoyInfo.java") to
   *     generated file content.
   * @throws SoySyntaxException If a syntax error is found.
   */
  ImmutableMap<String, String> generateParseInfo() throws SoySyntaxException;

  /** The Soy template code. */
  String getCode();

  /** The directory of the Soy source file. */
  String getDirectory();

  /** The source file name with no file extension. */
  String getNameNoExtension();

  /** The Soy file namespace. */
  String getNamespace();

  /**
   * A set containing the namespace declared by this Soy source file. This
   * method exists to be consistent with the {@link ProvidesRequiresSourceFile}
   * interface, which is needed for the dependency management tools provided
   * in the {@link org.closureant.deps} package.
   */
  Collection<String> getProvides();

  /** The set of namespaces required by this Soy source file. */
  Collection<String> getRequires();
}