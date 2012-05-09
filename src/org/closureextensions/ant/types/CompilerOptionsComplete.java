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

package org.closureextensions.ant.types;

import java.io.File;

/**
 * Object providing the Closure Compiler options in a format adhering to the
 * Apache Ant design pattern for XML attributes and nested elements. With the
 * exception of the flags listed below, all Compiler options are supported as
 * defined in <a target="_blank"
 * href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CommandLineRunner.java">
 * CommandLineRunner.java</a>.
 *
 * <p><b>Unsupported Compiler Flags</b>
 * <ul>
 * <li>--closure_entry_point</li>
 * <li>--js</li>
 * <li>--js_output_file</li>
 * <li>--jscomp_dev_mode</li>
 * <li>--manage_closure_dependencies</li>
 * <li>--only_closure_dependencies</li>
 * <li>--output_manifest</li>
 * <li>--version</li>
 * </ul></p>
 *
 * <p>These options are not supported for the following reasons:</p>
 *
 * <p><ul>
 * <li>The option may be implemented at the Ant-task level (for example, see
 * {@link org.closureextensions.ant.ClosureCompilerTask}, which supports specifying
 * JavaScript sources, a JavaScript output file, and an output manifest).
 * The decision to reserve certain options for Ant tasks to implement is
 * to reduce redundancy in cases where the task may provide an API better
 * suited to Ant-task design conventions versus a traditional command line
 * interface.</li>
 * <li>The option offers minimal value in the context of a build process (for
 * example, {@code --version})</li>
 * <li>{@link CompilerOptionsComplete} includes the nested element
 * {@literal <flag>} that provides a backdoor to pass any Compiler flag
 * defined in <a target="_blank"
 * href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CommandLineRunner.java">
 * CommandLineRunner.java</a> (see example below)</li>
 * </ul></p>
 *
 * <p><b>Flag Example for {@code --jscomp_dev_mode}</b></p>
 *
<p><pre>{@literal
<closure-compiler
    compilationLevel="SIMPLE_OPTIMIZATIONS"
    outputFile="app.js"
    onlyClosureDependencies="true">
  <flag name="--jscomp_dev_mode" value="START" />
  <namespaceentrypoint value="my.namespace" />
  <sources dir="${myapp.dir}" includes="*.js" />
</closure-compiler>
}</pre></p>
 *
 *
 * <p>The additional compiler options relative to {@link CompilerOptionsBasic}
 * are as follows:</p>
 *
 * <p><b>Ant Attributes</b></p>
 *
 * <p><ul>
 * <li>acceptConstKeyword</li>
 * <li>charset</li>
 * <li>commonJsModulePathPrefix</li>
 * <li>commonJsEntryModule</li>
 * <li>compilationLevel</li>
 * <li>createNameMapFiles</li>
 * <li>flagFile</li>
 * <li>generateExports</li>
 * <li>loggingLevel</li>
 * <li>moduleOutputPathPrefix</li>
 * <li>printAST</li>
 * <li>printPassGraph</li>
 * <li>printTree</li>
 * <li>processClosurePrimitives</li>
 * <li>processCommonJsModules</li>
 * <li>processjQueryPrimitives</li>
 * <li>sourceMapFormat</li>
 * <li>sourceMapOutputFile</li>
 * <li>summaryDetailLevel</li>
 * <li>thirdParty</li>
 * <li>transformAMDModules</li>
 * <li>translationsFile</li>
 * <li>translationsProject</li>
 * </ul></p>
 *
 * <p><b>Ant Nested Element</b></p>
 *
 * <p><ul>
 * <li>flags</li>
 * <li>module</li>
 * <li>moduleWrapper</li>
 * </ul></p>
 *
 *
 * * <ul class="blockList">
 * <li class="blockList">
 * <h3>Attributes</h3>
 *
 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0">
 * <col width="15%"/>
 * <col width="*"/>
 * <col width="*"/>
 * <col width="15%"/>
 * <thead>
 *   <tr><th>Attribute Name</th><th>Description</th><th>Required</th>
 *   <th>Equivalent Closure Compiler Flag</th></tr>
 * </thead>
 * <tbody>
 * <tr class="altColor"><td id="acceptConstKeyword"><b>acceptConstKeyword</b>
 *     </td><td>Allow usage of {@code const} keyword.</td><td>No. Defaults to
 *     {@code false}.</td><td>{@code --accept_const_keyword}</td></tr>
 * <tr class="rowColor"><td id="charset"><b>charset</b></td><td>The input and
 *     output charset for all files.</td><td>No. By default, the Closure
 *     Compiler accepts "UTF-8" as input and outputs "US_ASCII".</td>
 *     <td>{@code --charset}</td></tr>
 * <tr class="altColor"><td id="compilationLevel"><b>compilationLevel</b></td>
 *     <td>Specifies the compilation level to use. Options: "WHITESPACE_ONLY"
 *     (or "WHITESPACE"), "SIMPLE_OPTIMIZATIONS" (or "SIMPLE"),
 *     "ADVANCED_OPTIMIZATIONS" (or "ADVANCED").</td><td>No. Defaults to
 *     "SIMPLE_OPTIMIZATIONS".</td>
 *     <td>{@code --compilation_level}</td></tr>
 * <tr class="rowColor"><td id="createNameMapFiles"><b>createNameMapFiles</b>
 *     </td><td>If {@code true}, variable renaming and property renaming map
 *     files will be produced as {@literal {binary name}_vars_map.out} and
 *     {@literal {binary name}_props_map.out}. Note that this flag cannot be
 *     used in conjunction with either {@code variableMapOutputFile} or
 *     {@code propertyMapOutputFile}.</td><td>No. Defaults to {@code false}.
 *     </td><td>{@code --create_name_map_files}</td></tr>
 * <tr class="altColor"><td id="customExternsOnly"><b>customExternsOnly</b>
 *     </td><td>Whether only the custom externs specified by {@code externs}
 *     should be used (rather than in addition to the default externs bundled
 *     with the Closure Compiler).</td><td>No. Defaults to {@code false}.</td>
 *     <td>{@code --use_only_custom_externs}</td></tr>
 * <tr class="rowColor"><td id="debug"><b>debug</b></td><td>Enable debugging
 *     options.</td><td>No. Defaults to {@code false}.</td><td>{@code --debug}
 *     </td></tr>
 * <tr class="altColor"><td id="languageIn"><b>languageIn</b></td><td>The
 *     language specification that input sources conform. Options:
 *     "ECMASCRIPT3", "ECMASCRIPT5", "ECMASCRIPT5_STRICT".</td><td>No.
 *     Defaults to "ECMASCRIPT3".</td><td>{@code --language_in}</td></tr>
 * <tr class="rowColor"><td id="outputWrapper"><b>outputWrapper</b></td><td>
 *     A template into which compiled JavaScript will be written. The
 *     placeholder for compiled code is {@code %output%}.</td><td>No.</td>
 *     <td>{@code --output_wrapper}</td></tr>
 * <tr class="altColor"><td id="prettyPrint"><b>prettyPrint</b></td><td>
 *     "Pretty print" the output using line breaks and indentation to make
 *     the code easier to read.</td><td>No. Defaultsto {@code false}.</td>
 *     <td>{@code --formatting PRETTY_PRINT}</td></tr>
 * <tr class="rowColor"><td id="printInputDelimiter"><b>printInputDelimiter
 *     </b></td><td>Insert a comment of the form "{@code // Input X}" at the
 *     start of each file boundary where {@code X} is a number starting with
 *     zero.
 *
 *     <p>Example (from <a target="_blank"
 *     href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
 *     Definitive Guide</i></a>):</p>
 *     <p><pre>
 * // Input 0
 * alert("I am a statement from the first input file");
 * // Input 1
 * alert("I am a statement from the second input file");
 *     </pre></p>
 *     </td><td>No. Defaults to {@code false}.</td>
 *     <td>{@code --formatting PRINT_INPUT_DELIMITER}</td></tr>
 * <tr class="altColor"><td id="propertyMapInputFile"><b>propertyMapInputFile
 *     </b></td><td>File containing the serialized version of the property
 *     renaming map produced by a previous compilation</td><td>No.</td>
 *     <td>{@code --property_map_input_file}</td></tr>
 * <tr class="rowColor"><td id="propertyMapOutputFile"><b>
 *     propertyMapOutputFile</b></td><td>File where the serialized version of
 *     the property renaming map produced should be saved.</td><td>No.</td>
 *     <td>{@code --property_map_output_file}</td></tr>
 * <tr class="altColor"><td id="sourceMapFormat"><b>sourceMapFormat</b></td>
 *     <td>The source map format to produce. Options: "V1", "V2", "V3",
 *     "DEFAULT".</td><td>No. Defaults to "DEFAULT", which produces "V2".</td>
 *     <td>{@code --source_map_format}</td></tr>
 * <tr class="rowColor"><td id="sourceMapOutputFile"><b>sourceMapOutputFile
 *     </b></td><td>File where the mapping from compiled code back to
 *     original source code should be saved. The {@code %outname%}
 *     placeholder will expand to the name of the output file that the source
 *     map corresponds to.</td><td>No.</td><td>{@code --create_source_map}
 *     </td></tr>
 * <tr class="altColor"><td id="thirdParty"><b>thirdParty</b></td><td>Check
 *     source validity but do not enforce Closure style rules and conventions.
 *     </td><td>No. Defaults to {@code false}.</td><td>{@code --third_party}
 *     </td></tr>
 * <tr class="rowColor"><td id="variableMapInputFile"><b>variableMapInputFile
 *     </b></td><td>File containing the serialized version of the variable
 *     renaming map produced by a previous compilation.</td><td>No.</td>
 *     <td>{@code --variable_map_input_file}</td></tr>
 * <tr class="altColor"><td id="variableMapOutputFile"><b>
 *     variableMapOutputFile</b></td><td>File where the serialized version of
 *     the variable renaming map produced should be saved</td><td>No.</td>
 *     <td>{@code --variable_map_output_file}</td></tr>
 * <tr class="rowColor"><td id="warningLevel"><b>warningLevel</b></td><td>The
 *     warning level, which must be one of "QUIET", "DEFAULT",
 *     or "VERBOSE".</td><td>No. Defaults to "DEFAULT".</td><td>
 *     {@code --warning_level}</td></tr>
 * </tbody>
 * </table>
 * </li>
 * </ul>
 *
 *
 * <ul class="blockList">
 * <li class="blockList">
 * <h3>Nested Elements</h3>
 *
 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0">
 * <col width="15%"/>
 * <col width="*"/>
 * <col width="15%"/>
 * <thead>
 * <tr><th>Element Name</th><th>Description</th>
 * <th>Equivalent Closure Compiler Flag</th></tr>
 * </thead>
 * <tbody>
 *
 * <tr class="altColor"><td id="compilerCheck"><b>compilerCheck</b></td><td>
 *     A name-value pair where the name is a Closure Compiler check and the
 *     value is a check level. The supported checks correspond to the
 *     diagnostic groups defined in <a target="_blank"
 *     href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/DiagnosticGroups.java">
 *     DiagnosticGroups.java</a>. The check level must be one of "OFF",
 *     "WARNING", or "ERROR".</td><td>{@code --jscomp_error},<br>
 *     {@code --jscomp_warning},<br>{@code --jscomp_off}</td></tr>
 * <tr class="rowColor"><td id="define"><b>define</b></td><td>A compile-time
 *     override for a JavaScript variable annotated with {@code @define}. The
 *     {@code type} field must be set to one of "boolean", "number",
 *     or "string". If the type is "string", then the {@code value} field
 *     will be automatically quoted.
 *
 *     <p>For more information about compile-time defines, see:
 *     <a target="_blank" href="http://www.amazon.com/gp/product/1449381871">
 *     <i>Closure: The Definitive Guide</i></a> page 353.</p></td>
 *     <td>{@code --define}</td></tr>
 * <tr class="altColor"><td id="externs"><b>externs</b></td><td>A file that
 *     contains externs that should be included in the compilation. By default,
 *     these will be used in addition to the default externs bundled with the
 *     Closure Compiler.</td><td>{@code --externs}</td></tr>
 * <tr class="rowColor"><td id="flag"><b>flag</b></td><td>The {@literal <flag>}
 *     nested element provides a back door to directly pass Closure Compiler
 *     command-line flags that may or not already be provided as Ant task
 *     attributes or nested elements. The flags must be formatted as defined
 *     in <a target="_blank"
 *     href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CommandLineRunner.java">
 *     CommandLineRunner.java</a>.</td><td>N/A</td></tr>
 * </tbody>
 * </table>
 * </li>
 * </ul>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public interface CompilerOptionsComplete extends CompilerOptionsBasic {
  /*
  // Attributes

  Boolean acceptConstKeyword;
  String charset;
  String commonJsModulePathPrefix;
  String commonJsEntryModule;
  CompilationLevel compilationLevel;
  Boolean createNameMapFiles;
  File flagFile;
  Boolean generateExports;
  Level loggingLevel;
  String moduleOutputPathPrefix;
  File outputFile;
  Boolean printAST;
  Boolean printPassGraph;
  Boolean printTree;
  Boolean processClosurePrimitives;
  Boolean processCommonJsModules;
  Boolean processjQueryPrimitives;
  String sourceMapFormat;
  String sourceMapOutputFile;
  Integer summaryDetailLevel;
  Boolean thirdParty;
  Boolean transformAMDModules;
  File translationsFile;
  String translationsProject;

  // Nested elements

  final CommandLineBuilder flags;
  final List<StringNestedElement> modules;
  final List<StringNestedElement> moduleWrappers;

  */

  // Attribute setters

  /**
   * @param acceptConstKeyword allow usage of {@code const} keyword
   */
  void setAcceptConstKeyword(boolean acceptConstKeyword);

  /**
   * @param charset the input and output charset for all files. By default,
   *     the Closure Compiler accepts "UTF-8" as input and outputs "US_ASCII".
   */
  void setCharset(String charset);

  /**
   * @param commonJsModulePathPrefix path prefix to be removed from Common JS
   *     module names
   */
  void setCommonJsModulePathPrefix(String commonJsModulePathPrefix);

  /**
   * @param commonJsEntryModule root of your common JS dependency hierarchy.
   *     Your main script.
   */
  void setCommonJsEntryModule(String commonJsEntryModule);

  /**
   * @param compilationLevel specifies the compilation level to use. Options:
   *     "WHITESPACE_ONLY" (or "WHITESPACE"), "SIMPLE_OPTIMIZATIONS" (or
   *     "SIMPLE"), "ADVANCED_OPTIMIZATIONS" (or "ADVANCED"). Defaults to
   *     "SIMPLE_OPTIMIZATIONS".
   * @throws org.apache.tools.ant.BuildException if {@code compilationLevel}
   *     is not a valid option
   */
  void setCompilationLevel(String compilationLevel);

  /**
   * @param createNameMapFiles if {@code true}, variable renaming and property
   *     renaming map files will be produced as
   *     {@literal {binary name}_vars_map.out} and
   *     {@literal {binary name}_props_map.out}. Note that this flag cannot be
   *     used in conjunction with either {@code variableMapOutputFile} or
   *     {@code propertyMapOutputFile}. Defaults to {@code false}.
   */
  void setCreateNameMapFiles(boolean createNameMapFiles);

  /**
   * @param flagFile a file containing additional command-line options
   */
  void setFlagFile(File flagFile);

  /**
   * @param generateExports generates export code for those marked with
   *     {@code @export}
   */
  void setGenerateExports(boolean generateExports);
  
  /**
   * @param loggingLevel the logging level (standard 
   *     {@link java.util.logging.Level} values) for Compiler progress. Does 
   *     not control errors or warnings for the JavaScript code under 
   *     compilation.
   * @throws NullPointerException if the loggingLevel is {@code null}
   * @throws IllegalArgumentException if the value is not valid. Valid values
   *     are integers between Integer.MIN_VALUE and Integer.MAX_VALUE, and all
   *     known level names.
   * @see java.util.logging.Level
   */
  void setLoggingLevel(String loggingLevel);

  /**
   * Prefix for file names of compiled js modules. {@literal {module-name}.js}
   * will be appended to this prefix. Directories will be created as needed.
   * Use with the nested element {@literal <module>}.
   *
   * @param moduleOutputPathPrefix the module output prefix
   */
  void setModuleOutputPathPrefix(String moduleOutputPathPrefix);

  /**
   * @param printAST prints a dot file describing the internal abstract syntax
   *     tree and exits
   */
  void setPrintAST(boolean printAST);

  /**
   * @param printPassGraph prints a dot file describing the passes that will
   *     get run and exits
   */
  void setPrintPassGraph(boolean printPassGraph);

  /**
   * @param printTree prints out the parse tree and exits
   */
  void setPrintTree(boolean printTree);

  /**
   * @param processClosurePrimitives processes built-ins from the Closure
   *     library, such as {@code goog.require()}, {@code goog.provide()},
   *     and {@code goog.exportSymbol()}
   */
  void setProcessClosurePrimitives(boolean processClosurePrimitives);

  /**
   * @param processCommonJsModules process Common JS modules to a
   *     concatenable form
   */
  void setProcessCommonJsModules(boolean processCommonJsModules);

  /**
   * @param processjQueryPrimitives processes built-ins from the jQuery
   *     library, such as {@code jQuery.fn} and {@code jQuery.extend()}
   */
  void setProcessjQueryPrimitives(boolean processjQueryPrimitives);

  /**
   * @param sourceMapFormat the source map format to produce. Options: "V1",
   *     "V2", "V3", "DEFAULT". Defaults to "DEFAULT", which produces "V2".
   * @throws org.apache.tools.ant.BuildException if {@code sourceMapFormat} is
   *     not a valid option
   */
  void setSourceMapFormat(String sourceMapFormat);

  /**
   * @param sourceMapOutputFile file where the mapping from compiled code back
   *     to original source code should be saved. The {@code %outname%}
   *     placeholder will expand to the name of the output file that the
   *     source map corresponds to.
   */
  void setSourceMapOutputFile(String sourceMapOutputFile);

  /**
   * Controls how detailed the compilation summary is. Values: 0 (never print
   * summary), 1 (print summary only if there are errors or warnings), 2
   * (print summary if type checking is on, see nested element
   * {@literal <compilerCheck>}, 3 (always print summary). The default level
   * is 1.
   *
   * @param summaryDetailLevel the summary detail level
   */
  void setSummaryDetailLevel(int summaryDetailLevel);

  /**
   * @param thirdParty check source validity but do not enforce Closure style
   *     rules and conventions. Defaults to {@code false}.
   */
  void setThirdParty(boolean thirdParty);

  /**
   * @param transformAMDModules transform AMD to Common JS modules
   */
  void setTransformAMDModules(boolean transformAMDModules);

  /**
   * @param translationsFile source of translated messages. Currently only
   *     supports XTB.
   */
  void setTranslationsFile(File translationsFile);

  /**
   * Scopes all translations to the specified project. When specified, we will
   * use different message ids so that messages in different projects can have
   * different translations.
   *
   * @param translationsProject the project to which translations are to be
   *     scoped
   */
  void setTranslationsProject(String translationsProject);


  // Nested element setters

  /**
   * The {@literal <flag>} nested element provides a back door to directly
   * pass Closure Compiler command-line flags that may or not already be
   * provided as Ant task attributes or nested elements. The flags must be
   * formatted as defined in <a target="_blank"
   * href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CommandLineRunner.java">
   * CommandLineRunner.java</a>.
   *
   * @param flag a name-value mapping from a Closure Compiler command-line
   *     flag to its value
   */
  void addConfiguredFlag(NameValuePair flag);

  /**
   * A javascript module specification. The format is "{@literal
   * <name>:<num-js-files>[:[<dep>,...][:]]]}". Module names must be unique.
   * Each dep is the name of a module that this module depends on. Modules
   * must be listed in dependency order, and js source files must be listed in
   * the corresponding order.
   *
   * @param module a module specification
   */
  void addModule(StringNestedElement module);

  /**
   * An output wrapper for a javascript module (optional). The format is
   * "{@literal <name>:<wrapper>}". The module name must correspond with a
   * module specified using the nested element {@literal <module>}. The
   * wrapper must contain {@code %s} as the code placeholder. The
   * {@code %basename%} placeholder can also be used to substitute the base
   * name of the module output file.
   *
   * @param moduleWrapper a module wrapper
   */
  void addModuleWrapper(StringNestedElement moduleWrapper);
}