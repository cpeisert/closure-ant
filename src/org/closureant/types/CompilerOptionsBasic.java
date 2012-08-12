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

package org.closureant.types;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Parameter;

import org.closureant.base.CommandLineBuilder;

import java.util.Set;

/**
 * Closure Compiler object providing a limited set of common compiler options
 * for Ant tasks and data types (e.g. for use as nested elements).
 *
 * <p>The provided compiler options are approximately the intersection of the
 * command line interface implemented by
 * <a href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CommandLineRunner.java">
 * CommandLineRunner.java</a> and the
 * <a href="http://plovr.com/options.html">plovr options</a>, excluding options
 * related to the location of JavaScript inputs, outputs, and their
 * dependencies such as {@code --js}, {@code --manage_closure_dependencies},
 * and {@code --closure_entry_point}. Functionality for specifying JavaScript
 * inputs and their dependencies should be implemented by Ant tasks that wrap
 * the Closure Compiler. See {@link org.closureant.ClosureCompiler},
 * {@link org.closureant.ClosureBuilderPython}, and
 * {@link org.closureant.Plovr}.</p>
 *
 * <p>The provided options are as follows:</p>
 *
 * <p><b>Ant Attributes</b></p>
 *
 * <p><ul>
 * <li>customExternsOnly</li>
 * <li>debug</li>
 * <li>languageIn</li>
 * <li>outputWrapper</li>
 * <li>prettyPrint</li>
 * <li>printInputDelimiter</li>
 * <li>propertyMapInputFile</li>
 * <li>propertyMapOutputFile</li>
 * <li>variableMapInputFile</li>
 * <li>variableMapOutputFile</li>
 * <li>warningLevel</li>
 * </ul></p>
 *
 * <p><b>Ant Nested Element</b></p>
 *
 * <p><ul>
 * <li>compilerCheck</li>
 * <li>define</li>
 * <li>externs</li>
 * </ul></p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public interface CompilerOptionsBasic {

  // Attribute setters

  /**
   * @param customExternsOnly whether only the custom externs specified by
   *     {@code externs} should be used (rather than in addition to the
   *     default externs bundled with the Closure Compiler). Defaults to
   *     {@code false}.
   */
  void setCustomExternsOnly(boolean customExternsOnly);

  /**
   * @param debug Enable debugging options. Defaults to {@code false}.
   */
  void setDebug(boolean debug);

  /**
   * @param languageIn the language specification that input sources conform.
   *     Options: "ECMASCRIPT3", "ECMASCRIPT5", "ECMASCRIPT5_STRICT". Defaults
   *     to "ECMASCRIPT3".
   * @throws org.apache.tools.ant.BuildException if {@code languageIn} is not a valid option
   */
  void setLanguageIn(String languageIn);

  /**
   * @param outputWrapper a template into which compiled JavaScript will be
   *     written; the placeholder for compiled code is {@code %output%}
   */
  void setOutputWrapper(String outputWrapper);

  /**
   * @param prettyPrint Equivalent to the command-line
   *     {@code --formatting=PRETTY_PRINT} flag for the Closure
   *     Compiler. Defaults to {@code false}.
   */
  void setPrettyPrint(boolean prettyPrint);

  /**
   * @param printInputDelimiter Equivalent to the command-line
   *     {@code --formatting=PRINT_INPUT_DELIMITER} flag for the Closure
   *     Compiler. Defaults to false.
   */
  void setPrintInputDelimiter(boolean printInputDelimiter);

  /**
   * @param inputFile file containing the serialized version of the property
   *     renaming map produced by a previous compilation
   */
  void setPropertyMapInputFile(String inputFile);

  /**
   * @param outputFile file where the serialized version of the property
   *     renaming map produced should be saved
   */
  void setPropertyMapOutputFile(String outputFile);

  /**
   * @param inputFile file containing the serialized version of the variable
   *     renaming map produced by a previous compilation
   */
  void setVariableMapInputFile(String inputFile);

  /**
   * @param outputFile file where the serialized version of the variable
   *     renaming map produced should be saved
   */
  void setVariableMapOutputFile(String outputFile);

  /**
   * @param warningLevel the warning level, which must be one of "QUIET",
   *     "DEFAULT", or "VERBOSE". Defaults to "DEFAULT".
   * @throws org.apache.tools.ant.BuildException if {@code warningLevel} is not a valid option
   */
  void setWarningLevel(String warningLevel);


  // Nested element setters

  /**
   * @param compilerCheck a name-value pair where the name is a Closure
   *     Compiler check and the value is a check level. The supported checks
   *     correspond to the diagnostic groups defined in
   *     <a href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/DiagnosticGroups.java">
   *     DiagnosticGroups.java</a>. The check
   *     level must be one of "OFF", "WARNING", or "ERROR".
   * @throws org.apache.tools.ant.BuildException if the check level is not a valid option
   */
  void addConfiguredCompilerCheck(NameValuePair compilerCheck);

  /**
   * For more information about compile-time defines, see: <a target="_blank"
   * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
   * Definitive Guide</i></a> page 353.
   *
   * @param define a compile-time override for a JavaScript variable
   *     annotated with {@code @define}. The {@code type} field must be set
   *     to one of "boolean", "number", or "string". If the type is "string",
   *     then the {@code value} field will be automatically quoted.
   * @throws org.apache.tools.ant.BuildException if the {@code name}, {@code value},
   *     or {@code type} field is {@code null}, or if the {@code type} field 
   *     is not one of "boolean", "number", or "string"  
   */
  void addConfiguredDefine(Parameter define);

  /**
   * @param externs a file that contains externs that should be included in the
   *     compilation. By default, these will be used in addition to the default
   *     externs bundled with the Closure Compiler.
   */
  void addExterns(FileSet externs);

  /**
   * Creates a {@link CommandLineBuilder} with flags and their associated
   * values based on the compiler options set in this object. The flags must
   * be formatted as defined in <a target="_blank"
   * href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CommandLineRunner.java">
   * CommandLineRunner.java</a>.
   *
   * @return a {@link CommandLineBuilder} with flags and their associated
   *     values based on the compiler options set in this object
   */
  CommandLineBuilder getCommandLineFlags(Project project);

  /**
   * Get the set of extern files.
   *
   * @param project the Ant project
   * @return a {@link Set} of extern files or an empty set if no externs
   *     specified
   */
  Set<String> getExterns(Project project);
}
