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

import com.google.gson.JsonPrimitive;

import org.closureant.base.CommandLineBuilder;
import org.closureant.types.NameValuePair;
import org.closureant.util.StringUtil;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

import java.util.Map;

/**
 * // TODO(cpeisert): Delete this file once documentation has been migrated.
 *
 * Closure Compiler object providing common compiler options. May be used to
 * implement nested {@literal <compiler>} elements within Ant tasks or as a
 * parent class for tasks that wrap the Closure Compiler jar file.
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
 * <tr class="rowColor"><td id="flag"><b>flag</b></td><td>Allows setting
 *     Closure Compiler flags that are not provided as attributes or nested
 *     elements (if any). The flag names must be formatted as defined
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
public class ClosureCompiler extends AbstractClosureCompiler {
  
  // Attributes

  protected Boolean acceptConstKeyword;
  protected String charset;
  protected CompilationLevel compilationLevel;
  protected Boolean createNameMapFiles;
  protected String sourceMapFormat;
  protected String sourceMapOutputFile;
  protected Boolean thirdParty;


  // Nested elements

  protected final CommandLineBuilder flags;


  /**
   * Constructs a new {@literal <compiler>} nested element for use with the 
   * {@link org.closureant.ClosureBuilderPythonTask}.
   */
  public ClosureCompiler() {
    super();

    // Attributes

    this.acceptConstKeyword = null;
    this.charset = "";
    this.compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    this.createNameMapFiles = null;
    this.outputWrapper = "";
    this.sourceMapFormat = "";
    this.sourceMapOutputFile = "";
    this.thirdParty = null;

    // Nested elements

    this.flags = new CommandLineBuilder();
  }


  // Attribute setters

  /**
   * @param acceptConstKeyword allow usage of {@code const} keyword
   */
  public void setAcceptConstKeyword(boolean acceptConstKeyword) {
    this.acceptConstKeyword = acceptConstKeyword;
  }

  /**
   * @param charset the input and output charset for all files. By default,
   *     the Closure Compiler accepts "UTF-8" as input and outputs "US_ASCII".
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }

  /**
   * @param compilationLevel specifies the compilation level to use. Options:
   *     "WHITESPACE_ONLY" (or "WHITESPACE"), "SIMPLE_OPTIMIZATIONS" (or 
   *     "SIMPLE"), "ADVANCED_OPTIMIZATIONS" (or "ADVANCED"). Defaults to
   *     "SIMPLE_OPTIMIZATIONS".
   * @throws BuildException if {@code compilationLevel} is not a valid option
   */
  public void setCompilationLevel(String compilationLevel) {
    String compilationLevelUpperCase = compilationLevel.toUpperCase();
    
    if (compilationLevelUpperCase.startsWith("WHITESPACE")) {
      this.compilationLevel = CompilationLevel.WHITESPACE_ONLY;
    } else if (compilationLevelUpperCase.startsWith("SIMPLE")) {
      this.compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    } else if (compilationLevelUpperCase.startsWith("ADVANCED")) {
      this.compilationLevel = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    } else {
      throw new BuildException("compilationLevel expected to be one of "
          + "\"WHITESPACE_ONLY\" (or \"WHITESPACE\"), "
          + "\"SIMPLE_OPTIMIZATIONS\" (or \"SIMPLE\"), " +
          "or \"ADVANCED_OPTIMIZATIONS\" (or \"ADVANCED\"), "
          + "but was \"" + compilationLevelUpperCase + "\"");
    }
  }

  /**
   * @param createNameMapFiles if {@code true}, variable renaming and property 
   *     renaming map files will be produced as 
   *     {@literal {binary name}_vars_map.out} and 
   *     {@literal {binary name}_props_map.out}. Note that this flag cannot be 
   *     used in conjunction with either {@code variableMapOutputFile} or
   *     {@code propertyMapOutputFile}. Defaults to {@code false}.
   */
  public void setCreateNameMapFiles(boolean createNameMapFiles) {
    this.createNameMapFiles = createNameMapFiles;
  }

  /**
   * @param sourceMapFormat the source map format to produce. Options: "V1",
   *     "V2", "V3", "DEFAULT". Defaults to "DEFAULT", which produces "V2".
   * @throws BuildException if {@code sourceMapFormat} is not a valid option
   */
  public void setSourceMapFormat(String sourceMapFormat) {
    if ("V1".equalsIgnoreCase(sourceMapFormat)
        || "V2".equalsIgnoreCase(sourceMapFormat)
        || "V3".equalsIgnoreCase(sourceMapFormat)
        || "DEFAULT".equalsIgnoreCase(sourceMapFormat)) {
      this.sourceMapFormat = sourceMapFormat;
    } else {
      throw new BuildException("sourceMapFormat expected to be one of "
          + "\"V1\", \"V2\", \"V3\", or \"DEFAULT\" but was \""
          + sourceMapFormat.toUpperCase() + "\"");
    }
  }

  /**
   * @param sourceMapOutputFile file where the mapping from compiled code back
   *     to original source code should be saved. The {@code %outname%}
   *     placeholder will expand to the name of the output file that the
   *     source map corresponds to.
   */
  public void setSourceMapOutputFile(String sourceMapOutputFile) {
    this.sourceMapOutputFile = sourceMapOutputFile;
  }

  /**
   * @param thirdParty check source validity but do not enforce Closure style
   *     rules and conventions. Defaults to {@code false}.
   */
  public void setThirdParty(boolean thirdParty) {
    this.thirdParty = thirdParty;
  }


  // Nested element setters

  /**
   * Allows setting Closure Compiler flags that are not provided as attributes
   * or nested elements (if any). The flag names must be formatted as defined
   * in <a target="_blank"
   * href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CommandLineRunner.java">
   * CommandLineRunner.java</a>.
   *
   * @param flag a name-value mapping from a Closure Compiler flag to its value
   */
  public void addConfiguredFlag(NameValuePair flag) {
    this.flags.flagAndArgument(flag.getName(), flag.getValue());
  }

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
  public CommandLineBuilder getCommandLineFlags() {
    CommandLineBuilder cmdline = this.flags;

    // Attributes inherited from AbstractClosureCompiler

    if (customExternsOnly != null) {      
      cmdline.flagAndArgument("--use_only_custom_externs", 
          customExternsOnly.toString());
    }
    if (debug != null) {
      cmdline.flagAndArgument("--debug", debug.toString());
    }
    if (!languageIn.isEmpty()) {
      cmdline.flagAndArgument("--language_in", languageIn);
    }
    if (!outputWrapper.isEmpty()) {
      cmdline.flagAndArgument("--output_wrapper", outputWrapper);
    }
    if (Boolean.TRUE.equals(prettyPrint)) {
      cmdline.flagAndArgument("--formatting", "PRETTY_PRINT");
    }
    if (Boolean.TRUE.equals(printInputDelimiter)) {
      cmdline.flagAndArgument("--formatting", "PRINT_INPUT_DELIMITER");
    }
    if (!propertyMapInputFile.isEmpty()) {
      cmdline.flagAndArgument("--property_map_input_file",
          propertyMapInputFile);
    }
    if (!propertyMapOutputFile.isEmpty()) {
      cmdline.flagAndArgument("--property_map_output_file",
          propertyMapOutputFile);
    }
    if (!variableMapInputFile.isEmpty()) {
      cmdline.flagAndArgument("--variable_map_input_file",
          variableMapInputFile);
    }
    if (!variableMapOutputFile.isEmpty()) {
      cmdline.flagAndArgument("--variable_map_output_file",
          variableMapOutputFile);
    }
    if (!warningLevel.isEmpty()) {
      cmdline.flagAndArgument("--warning_level", warningLevel);
    }

    // Attributes implemented in ClosureCompiler

    if (acceptConstKeyword != null) {
      cmdline.flagAndArgument("--accept_const_keyword",
          acceptConstKeyword.toString());
    }
    if (!charset.isEmpty()) {
      cmdline.flagAndArgument("--charset", charset);
    }
    cmdline.flagAndArgument("--compilation_level", compilationLevel.toString());
    if (createNameMapFiles != null) {
      cmdline.flagAndArgument("--create_name_map_files",
          createNameMapFiles.toString());
    }
    if (!sourceMapFormat.isEmpty()) {
      cmdline.flagAndArgument("--source_map_format", sourceMapFormat);
    }
    if (!sourceMapOutputFile.isEmpty()) {
      cmdline.flagAndArgument("--create_source_map", sourceMapOutputFile);
    }
    if (thirdParty != null) {
      cmdline.flagAndArgument("--third_party", thirdParty.toString());
    }


    // Nested elements inherited from AbstractClosureCompiler

    for (NameValuePair pair : compilerChecks) {
      String checkLevelUpperCase = pair.getValue().toUpperCase();
      if ("OFF".equals(checkLevelUpperCase)) {
        cmdline.flagAndArgument("--jscomp_off", pair.getName());
      } else if ("WARNING".equals(checkLevelUpperCase)) {
        cmdline.flagAndArgument("--jscomp_warning", pair.getName());
      } else if ("ERROR".equals(checkLevelUpperCase)) {
        cmdline.flagAndArgument("--jscomp_error", pair.getName());
      } else {
        throw new BuildException("compiler check level expected to be one of "
            + "\"OFF\", \"WARNING\", or \"ERROR\" but was \""
            + checkLevelUpperCase + "\"");
      }
    }

    for (Map.Entry<String, JsonPrimitive> define : defines.entrySet()) {
      String value = define.getValue().getAsString();

      if (define.getValue().isString()) {
        value = StringUtil.quoteString(value);
      }
      cmdline.flagAndArgument("--define", define.getKey() + "=" + value);
    }
    
    for (FileSet externFiles : externs) {
      cmdline.flagAndFileSet("--externs", externFiles, getProject());
    }

    return cmdline;
  }
}