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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;

import org.closureextensions.ant.PlovrConfig;

/**
 * Closure Compiler object providing compiler options for nested
 * {@literal <compiler>} elements within the {@link
 * org.closureextensions.ant.PlovrTask} Ant task.
 *
 * <ul class="blockList">
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
 *
 * <tr class="altColor"><td><b>ambiguateProperties</b></td><td>Corresponds to
 *     the <i>internal</i> Closure Compiler Java option of the same name,
 *     which determines if the Compiler should use type information to
 *     ambiguate properties. When the Compiler can determine that two
 *     properties belong to different objects, then the properties may be
 *     given the same names (i.e. made ambiguous). This results in more
 *     properties being given short names (e.g. one letter), which reduces
 *     code size and helps improve gzip compression because the same names
 *     are more commonly used.
 *
 *     <p>See the excellent example in <a target="_blank"
 *     href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
 *     Definitive Guide</i></a> on page 447.</p></td><td>No. Defaults to
 *     {@code false}.</td><td>N/A</td></tr>
 * <tr class="rowColor"><td><b>compilationLevel</b></td><td>Specifies the
 *     compilation level to use. Options: "RAW", "WHITESPACE"
 *     (or "WHITESPACE_ONLY"), "SIMPLE" (or "SIMPLE_OPTIMIZATIONS"),
 *     "ADVANCED" (or "ADVANCED_OPTIMIZATIONS").
 *
 *     <p><b>Note:</b> "RAW" works with the plovr "serve" mode but
 *     does not work in "build" mode. See <a target="_blank"
 *     href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
 *     Definitive Guide</i></a> page 535.</p></td><td>No. Defaults to "SIMPLE".
 *     </td><td>{@code --compilation_level}</td></tr>
 * <tr class="altColor"><td><b>customExternsOnly</b></td><td>Whether only the
 *     custom externs specified by {@code externs} should be used (rather
 *     than in addition to the default externs bundled with the Closure
 *     Compiler).</td><td>No. Defaults to {@code false}.</td>
 *     <td>{@code --use_only_custom_externs}</td></tr>
 * <tr class="rowColor"><td><b>debug</b></td><td>Enable debugging options.
 *     </td><td>No. Defaults to {@code false}.</td><td>{@code --debug}</td></tr>
 * <tr class="altColor"><td><b>disambiguateProperties</b></td><td>Corresponds
 *     to the <i>internal</i> Closure Compiler Java option of the same name,
 *     determines if the Compiler should rename properties to disambiguate
 *     between unrelated fields with the same name. For example, if a property
 *     in one of your JavaScript objects collided with a name in an externs
 *     file, then without disambiguating properties, the Compiler would not
 *     be able to rename the colliding property to a shorter name.</p>
 *
 *     <p>See the excellent example in <a target="_blank"
 *     href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
 *     Definitive Guide</i></a> on page 445.</p></td><td>No. Defaults to
 *     {@code false}.</td><td>N/A</td></tr>
 * <tr class="rowColor"><td><b>languageIn</b></td><td>The language
 *     specification that input sources conform. Options: "ECMASCRIPT3",
 *     "ECMASCRIPT5", "ECMASCRIPT5_STRICT".</td><td>No. Defaults to
 *     "ECMASCRIPT3".</td><td>{@code --language_in}</td></tr>
 * <tr class="altColor"><td><b>outputCharset</b></td><td>If you use a lot of
 *     international characters in your strings, then you may want to
 *     consider setting this to "UTF-8". Though if you do so, in order to make
 *     sure that your JavaScript code is interpreted with the correct
 *     character encoding, make sure to specify it in the {@literal <script>}
 *     tag as follows:
 *
 *     <p><pre>{@literal
 * <script type="text/javascript" src="myscript.js" charset="utf-8"></script>
 *     }</pre></p>
 *     </td><td>No. Defaults to "US-ASCII".</td><td>{@code --charset}</td></tr>
 * <tr class="rowColor"><td><b>outputWrapper</b></td><td>A template into which
 *     compiled JavaScript will be written. The placeholder for compiled code
 *     is {@code %output%}. If the nested element {@literal <outputWrapper>}
 *     is used in addition to the {@code outputWrapper} attribute, the text
 *     contained in the nested elements is appended to the output wrapper
 *     attribute text.</td><td>No.</td><td>{@code --output_wrapper}</td></tr>
 * <tr class="altColor"><td><b>prettyPrint</b></td><td>"Pretty print" the
 *     output using line breaks and indentation to make the code easier to
 *     read.</td><td>No. Defaults to {@code false}.</td>
 *     <td>{@code --formatting PRETTY_PRINT}</td></tr>
 * <tr class="rowColor"><td><b>printInputDelimiter</b></td><td>Insert a
 *     comment of the form "{@code // Input X}" at the start of each file
 *     boundary where {@code X} is a number starting with zero.
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
 * <tr class="altColor"><td><b>propertyMapInputFile</b></td><td>File
 *     containing the serialized version of the property renaming map produced
 *     by a previous compilation</td><td>No.</td>
 *     <td>{@code --property_map_input_file}</td></tr>
 * <tr class="rowColor"><td><b>propertyMapOutputFile</b></td><td>File where
 *     the serialized version of the property renaming map produced should be
 *     saved.</td><td>No.</td><td>{@code --property_map_output_file}</td></tr>
 * <tr class="altColor"><td><b>sourceMapFormat</b></td><td>The source map
 *     format to produce. Options: "V1", "V2", "V3", "DEFAULT".</td><td>No.
 *     Defaults to "DEFAULT", which produces "V2".</td>
 *     <td>{@code --source_map_format}</td></tr>
 * <tr class="rowColor"><td><b>sourceMapOutputFile</b></td><td>File where the
 *     mapping from compiled code back to original source code should be
 *     saved. The {@code %outname%} placeholder will expand to the name of
 *     the output file that the source map corresponds to.</td><td>No.</td>
 *     <td>{@code --create_source_map}</td></tr>
 * <tr class="altColor"><td><b>treatWarningsAsErrors</b></td><td>When set to
 *     {@code true}, warnings will be reported as errors. Compilation will
 *     still succeed if there are warnings, but will fail if there are any
 *     errors, so enabling this option will draw more attention to any
 *     potential problems detected by the Closure Compiler.</td><td>No.
 *     Defaults to {@code false}.</td><td>N/A</td></tr>
 * <tr class="rowColor"><td><b>variableMapInputFile</b></td><td>File
 *     containing the serialized version of the variable renaming map
 *     produced by a previous compilation.</td><td>No.</td>
 *     <td>{@code --variable_map_input_file}</td></tr>
 * <tr class="altColor"><td><b>variableMapOutputFile</b></td><td>File where
 *     the serialized version of the variable renaming map produced should be
 *     saved</td><td>No.</td><td>{@code --variable_map_output_file}</td></tr>
 * <tr class="rowColor"><td><b>warningLevel</b></td><td>The warning level,
 *     which must be one of "QUIET", "DEFAULT", or "VERBOSE".</td><td>No.
 *     Defaults to "DEFAULT".</td><td>{@code --warning_level}</td></tr>
 * </tbody>
 * </table>
 * </li>
 * </ul>
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
 * <tr class="altColor"><td><b>compilerCheck</b></td><td>A name-value pair
 *     where the name is a Closure Compiler check and the value is a check
 *     level. The supported checks correspond to the diagnostic groups defined
 *     in <a target="_blank"
 *     href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/DiagnosticGroups.java">
 *     DiagnosticGroups.java</a>. The check level must be one of "OFF",
 *     "WARNING", or "ERROR".</td><td>
 *     {@code --jscomp_error},<br>
 *     {@code --jscomp_warning},<br>
 *     {@code --jscomp_off}</td></tr>
 * <tr class="rowColor"><td><b>define</b></td><td>A compile-time override for
 *     a JavaScript variable annotated with {@code @define}. The {@code type}
 *     field must be set to one of "boolean", "number", or "string". If the
 *     type is "string", then the {@code value} field will be automatically
 *     quoted.</td><td>{@code --define}</td></tr>
 * <tr class="altColor"><td><b>experimentalCompilerOptions</b></td><td>Enables
 *     setting Closure Compiler options defined in <a target="_blank"
 *     href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CompilerOptions.java">
 *     CompilerOptions.java</a> that may not be available via the command line
 *     interface.
 *
 *     <p>See <a target="_blank"
 *     href="http://plovr.com/options.html#experimental-compiler-options">
 *     experimental-compiler-options</a> on plovr.com.</p></td><td>N/A</td></tr>
 * <tr class="rowColor"><td><b>externs</b></td><td>A file that contains
 *     externs that should be included in the compilation. By default, these
 *     will be used in addition to the default externs bundled with the Closure
 *     Compiler.</td><td>{@code --externs}</td></tr>
 * <tr class="altColor"><td><b>idGenerators</b></td><td>Corresponds to the
 *     <i>internal</i> Closure Compiler Java option {@code setIdGenerators},
 *     which specifies a set of functions that generate unique ids and
 *     replaces their calls with the ids themselves. It is common to set this
 *     option as follows so that all event ids are unique:
 *
 *     <p><pre>{@literal
 * <idgenerator value="goog.events.getUniqueId" />
 *     }</pre></p>
 *
 *     <p>See page 444 of <a target="_blank"
 *     href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
 *     Definitive Guide</i></a> for details.</p></td><td>N/A</td></tr>
 * <tr class="rowColor"><td><b>nameSuffixesToStrip</b></td><td>Corresponds to
 *     the <i>internal</i> Closure Compiler Java option
 *     {@code stripNameSuffixes}, which specifies a set of suffixes for
 *     variable or property names that the Compiler will remove from the
 *     source code. This is commonly used in conjunction with
 *     {@code stripTypePrefixes} to remove loggers as follows:
 *
 * <p><pre>
 * options.stripTypePrefixes = ImmutableSet.of("goog.debug", "goog.asserts");
 * options.stripNameSuffixes = ImmutableSet.of("logger", "logger_");
 * </pre></p>
 *
 *     <p>See page 443 of <a target="_blank"
 *     href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
 *     Definitive Guide</i></a> for details.</p></td><td>N/A</td></tr>
 * <tr class="altColor"><td><b>outputWrapper</b></td><td>See
 *     {@code outputWrapper} attribute above.</p></td>
 *     <td>{@code --output_wrapper}</td></tr>
 * <tr class="rowColor"><td><b>typePrefixesToStrip</b></td><td>Corresponds to
 *     the <i>internal</i> Closure Compiler Java option
 *     {@code stripTypePrefixes}, which specifies a set of prefixes that the
 *     Compiler will use to eliminate expressions in JavaScript. (The
 *     prefixes need not correspond to types, so the name of the option is a
 *     bit of a misnomer.) This is commonly used to remove debugging and
 *     assertion code by configuring the option as follows:
 *
 * <p><pre>
 * options.stripTypePrefixes = ImmutableSet.of("goog.debug", "goog.asserts");
 * </pre></p>
 *
 *     <p>See page 442 of <a target="_blank"
 *     href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
 *     Definitive Guide</i></a> for details.</p></td><td>N/A</td></tr>
 *
 * </tbody>
 * </table>
 * </li>
 * </ul>
 *
 * @author cpeisert@gmail.com (Christopher Peisert)
 */
public interface CompilerOptionsForPlovr extends CompilerOptionsBasic {

  /** Compilation modes supported by plovr. */
  public static enum PlovrCompilationMode {
    /**
     * Produces concatenation of raw source files. This mode is compatible
     * with plovr mode {@code serve} but does not work with plovr mode
     * {@code build}.
     */
    RAW,

    /** Equivalent to Closure Compiler compilation level "WHITESPACE_ONLY". */
    WHITESPACE,

    /**
     * Equivalent to Closure Compiler compilation level "SIMPLE_OPTIMIZATIONS".
     */
    SIMPLE,

    /**
     * Equivalent to Closure Compiler compilation level "ADVANCED_OPTIMIZATIONS".
     */
    ADVANCED,
    ;
  }

  /*
  // Attributes

  Boolean ambiguateProperties;
  PlovrCompilationMode compilationMode;
  Boolean disambiguateProperties;
  String outputCharset;
  String sourceMapFormat;
  String sourceMapOutputFile;
  Boolean treatWarningsAsErrors;


  // Nested elements

  final ExperimentalCompilerOptions experimentalCompilerOptions;
  final List<IdGenerator> idGenerators;
  final List<StringNestedElement> nameSuffixesToStrip;
  final List<StringNestedElement> outputWrapperNestedElement;
  final List<StringNestedElement> typePrefixesToStrip;
  */


  // Attribute setters

  /**
   * Documentation from <a target="_blank"
   * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
   * Definitive Guide</i></a> page 447.
   *
   * <p>{@code ambiguateProperties} corresponds to the internal Closure
   * Compiler option of the same name, which determines if the Compiler
   * should use type information to ambiguate properties. When the Compiler
   * can determine that two properties belong to different objects, then
   * the properties may be given the same names (i.e. made ambiguous). This
   * results in more properties being given short names (e.g. one letter),
   * which reduces code size and helps improve gzip compression because the
   * same names are more commonly used.</p>
   *
   * <p>See the excellent example in <a target="_blank"
   * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
   * Definitive Guide</i></a> on page 447.</p>
   *
   * @param ambiguateProperties determines if the Compiler should use type
   *     information to ambiguate properties
   */
  void setAmbiguateProperties(boolean ambiguateProperties);

  /**
   * @param compilationMode specifies the plovr compilation mode. Options:
   *     "RAW", "WHITESPACE" (or "WHITESPACE_ONLY"), "SIMPLE" (or
   *     "SIMPLE_OPTIMIZATIONS"), "ADVANCED" (or "ADVANCED_OPTIMIZATIONS").
   *     Defaults to "SIMPLE".
   * @throws org.apache.tools.ant.BuildException if {@code compilationMode} is
   *     not a valid option
   */
  void setCompilationMode(String compilationMode);

  /**
   * Documentation from <a target="_blank"
   * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
   * Definitive Guide</i></a> pages 445 to 447.
   *
   * <p>{@code disambiguateProperties} corresponds to the internal Closure
   * Compiler option of the same name. This option determines if the Compiler
   * should rename properties to disambiguate between unrelated fields with
   * the same name. For example, if a property in one of your JavaScript
   * objects collided with a name in an externs file, then without
   * disambiguating properties, the Compiler would not be able to rename the
   * property to a shorter name.</p>
   *
   * <p>See the excellent example in <a target="_blank"
   * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
   * Definitive Guide</i></a> on pages 445 to 447.</p>
   *
   * @param disambiguateProperties determines if the Compiler should use type
   *     information to disambiguate properties
   */
  void setDisambiguateProperties(boolean disambiguateProperties);

  /**
   * If you use a lot of international characters in your strings, then you
   * may want to consider setting this to "UTF-8". Though if you do so, in
   * order to make sure that your JavaScript code is interpreted with the
   * correct character encoding, make sure to specify it in the
   * {@literal <script>} tag as follows:
   *
   * <p><pre>{@literal
   * <script type="text/javascript" src="myscript.js" charset="utf-8"></script>
   * }</pre></p>
   *
   * @param outputCharset the output charset for all files. Defaults to
   *     "US-ASCII".
   */
  void setOutputCharset(String outputCharset);

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
   * Getter for the source map output file. This getter exists because as of
   * the February 2012 release of plovr, there is no config file option to
   * specify the source map output path. However, the plovr "build" command
   * does provide a command line flag {@code --create_source_map} to specify
   * where the source map should be written.
   *
   * @return the source map output file or {@code null} if not set
   */
  String getSourceMapOutputFile();

  /**
   * @param treatWarningsAsErrors When set to {@code true}, warnings will be
   *     reported as errors. Recall that compilation will still succeed if
   *     there are warnings, but will fail if there are any errors, so
   *     enabling this option will draw more attention to any potential
   *     problems detected by the Closure Compiler.
   */
  void setTreatWarningsAsErrors(boolean treatWarningsAsErrors);


  // Nested element setters

  /**
   * Enables setting Closure Compiler options defined in <a target="_blank"
   * href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CompilerOptions.java">
   * CompilerOptions.java</a> that may not be available via the command line
   * interface.
   *
   * <p>See <a target="_blank"
   * href="http://plovr.com/options.html#experimental-compiler-options">
   * experimental-compiler-options</a> on plovr.com.</p>
   *
   * @param compilerOption a Compiler option to set in <a target="_blank"
   *     href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CompilerOptions.java">
   *     CompilerOptions.java</a>
   */
  void addConfiguredExperimentalCompilerOption(Parameter compilerOption);

  /**
   * {@code idGenerator} corresponds to the internal Closure
   * Compiler option {@code setIdGenerators}, which specifies a set of
   * functions that generate unique ids and replaces their calls with the ids
   * themselves. It is common to set this option as follows so that all event
   * ids are unique:
   *
   * <p><pre>
   * {@literal <idgenerator value="goog.events.getUniqueId" />}
   * </pre></p>
   *
   * <p>See page 444 of <a target="_blank"
   * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
   * Definitive Guide</i></a> for details.</p>
   *
   * @param idGenerator
   */
  void addIdGenerator(IdGenerator idGenerator);

  /**
   * Documentation from <a target="_blank"
   * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
   * Definitive Guide</i></a> page 443.
   *
   * <p>{@code nameSuffixToStrip} corresponds to the internal Closure
   * Compiler option {@code stripNameSuffixes}, which specifies a set of
   * suffixes for variable or property names that the Compiler will remove
   * from the source code. This is commonly used in conjunction with
   * {@code stripTypePrefixes} to remove loggers as follows:</p>
   *
   * <p><pre>
   * options.stripTypePrefixes = ImmutableSet.of("goog.debug", "goog.asserts");
   * options.stripNameSuffixes = ImmutableSet.of("logger", "logger_");
   * </pre></p>
   *
   * @param nameSuffix a name suffix to strip
   */
  void addNameSuffixToStrip(StringNestedElement nameSuffix);

  /**
   * A template into which compiled JavaScript will be written. The
   * placeholder for compiled code is {@code %output%},
   * so to wrap the compiled output in an anonymous function preceded by a
   * copyright comment, specify:
   *
   * <p><pre>{@literal
   * <outputWrapper value="// Copyright 2012\n(function(){%output%})();" />
   * }</pre></p>
   *
   * <p>If the {@literal <compiler>} attribute {@code outputWrapper} is
   * specified in addition to nested {@literal <outputWrapper>} elements,
   * the nested element output wrappers will be appended to the output
   * wrapper specified by the {@literal <compiler>} attribute.</p>
   *
   * @param outputWrapper
   */
  void addOutputWrapper(StringNestedElement outputWrapper);

  /**
   * Documentation from <a target="_blank"
   * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
   * Definitive Guide</i></a> page 442.
   *
   * <p>{@code typePrefixToStrip} corresponds to the internal Closure Compiler
   * option {@code stripTypePrefixes}, which specifies a set of prefixes that
   * the Compiler will use to eliminate expressions in JavaScript. (The
   * prefixes need not correspond to types, so the name of the option is a
   * bit of a misnomer.) This is commonly used to remove debugging and
   * assertion code by configuring the option as follows:</p>
   *
   * <p><pre>
   * options.stripTypePrefixes = ImmutableSet.of("goog.debug", "goog.asserts");
   * </pre></p>
   *
   * @param typePrefix a type prefix to strip
   */
  void addTypePrefixToStrip(StringNestedElement typePrefix);

  /**
   * Creates a {@link org.closureextensions.ant.PlovrConfig} object with the compiler
   * options set based on the field values of this object.
   *
   * @param project the Ant project
   * @return a plovr config file object
   * @throws org.apache.tools.ant.BuildException on error
   */
  PlovrConfig toPlovrConfig(Project project);
}