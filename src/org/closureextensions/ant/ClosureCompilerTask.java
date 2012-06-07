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

package org.closureextensions.ant;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Parameter;

import org.closureextensions.ant.types.CompilationLevel;
import org.closureextensions.ant.types.CompilerOptionsComplete;
import org.closureextensions.ant.types.CompilerOptionsFactory;
import org.closureextensions.ant.types.NameValuePair;
import org.closureextensions.ant.types.StringNestedElement;
import org.closureextensions.common.util.AntUtil;
import org.closureextensions.common.util.ClosureBuildUtil;
import org.closureextensions.common.util.StringUtil;

/**
 * Closure Compiler Ant task. The default task name is {@code closure-compiler}
 * as defined in "task-definitions.xml".
 *
 * <p>The location of the Compiler jar file is also defined in
 * "closure-tools-config.xml", which should be included in your build file as
 * follows:</p>
 *
 * <p>{@literal <import file="your/path/to/closure-tools-config.xml" />}</p>
 *
 * <p><i>Verify that the paths defined in "closure-tools-config.xml" are
 * correct for your local configuration.</i></p>
 *
 * <p>The options "mainSources", "sources", and "sourceList" take the place of
 * the {@code --js} command line flag to exploit Ant's built-in capabilities.
 * "Inputs" additionally provides the convenience of automatically extracting
 * the {@code goog.provided} namespaces and passing them to the compiler using
 * {@code --closure_entry_point}. See {@link #addInputs(FileSet)} and
 * {@link #addNamespaceEntryPoint(StringNestedElement)}.</p>
 *
 *
 * TODO(cpeisert): Move the Ant-style documentation below into separate doc
 * files.
 *
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
 *     elements of the {@literal <compiler>} nested element, such as
 *     {@code --print_pass_graph}. The flag names must be formatted as defined
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
public final class ClosureCompilerTask extends Task
    implements CompilerOptionsComplete {

  private CompilerOptionsComplete compilerOptions;

  // Attributes
  private File compilerJar;
  private boolean forceRecompile;
  private String inputManifest;
  private Boolean manageClosureDependencies;
  private Boolean onlyClosureDependencies;
  private String outputFile;
  private String outputManifest;
  private boolean printCommandLine;

  // Nested elements
  private final List<FileSet> mainSources;
  private final List<StringNestedElement> namespaceEntryPoints;
  private final List<FileList> sourceLists;
  private final List<FileSet> sources;

  /**
   * Constructs a new Closure Compiler Ant task.
   */
  public ClosureCompilerTask() {
    this(null);
  }

  /**
   * Constructs a new bound Closure Compiler Ant task. This is useful when
   * using the {@link ClosureCompilerTask} within another task as follows:
   *
   * <p><pre>{@code
   * ClosureCompilerTask compilerTask = ClosureCompilerTask(this);
   * }</pre></p>
   */
  public ClosureCompilerTask(Task owner) {
    super();
    if (owner != null) {
      bindToOwner(owner);
    }
    this.compilerOptions = CompilerOptionsFactory.newCompilerOptionsComplete();

    // Attributes
    this.compilerJar = null;
    this.forceRecompile = false;
    this.inputManifest = null;
    this.manageClosureDependencies = null;
    this.onlyClosureDependencies = null;
    this.outputFile = null;
    this.outputManifest = null;
    this.printCommandLine = false;

    // Nested elements
    this.mainSources = Lists.newArrayList();
    this.namespaceEntryPoints = Lists.newArrayList();
    this.sourceLists = Lists.newArrayList();
    this.sources = Lists.newArrayList();
  }

  // Backdoor setter for tasks that wrap/compose the ClosureCompilerTask.
  public void protectedSetCompilerOptions(CompilerOptionsComplete options) {
    this.compilerOptions = options;
  }


  // Attribute setters

  /** @param file the Closure Compiler jar file */
  public void setCompilerJar(File file) {
    this.compilerJar = file;
  }

  /**
   * Forces recompilation even if the output JavaScript file is up-to-date.
   *
   * @param forceRecompile determines if the Closure Compiler should always
   *     recompile the {@code outputFile} even if none of the source files
   *     or compiler options have changed since the {@code outputFile} was
   *     last modified. Defaults to {@code false}.
   */
  public void setForceRecompile(boolean forceRecompile) {
    this.forceRecompile = forceRecompile;
  }

  /**
   * Specifies a file containing a list of JavaScript sources to be included
   * in the compilation, where each line in the manifest is a file path.
   *
   * <p><b>Note:</b> Sources specified in an input manifest always precede
   * sources specified with {@literal <sourceList>} and {@literal <sources>}.
   * See {@link #addSourceList(FileList)} and {@link #addSources(FileSet)}.
   * </p>
   *
   * @param inputManifest the input manifest file name
   */
  public void setInputManifest(String inputManifest) {
    this.inputManifest = inputManifest;
  }

  /**
   * Automatically sort dependencies so that a file that {@code goog.provides}
   * symbol X will always come before a file that {@code goog.requires} symbol
   * X. If an input provides symbols, and those symbols are never required,
   * then that input will not be included in the compilation.
   *
   * @param manageClosureDependencies determines if sources will be
   *     automatically topologically sorted. Defaults to {@code false}.
   */
  public void setManageClosureDependencies(boolean manageClosureDependencies) {
    this.manageClosureDependencies = manageClosureDependencies;
  }

  /**
   * Only include files in the transitive dependency of the entry points
   * (specified by closure_entry_point). Files that do not provide dependencies
   * will be removed. This supersedes {@code --manage_closure_dependencies}.
   *
   * @param onlyClosureDependencies determines if sources will be
   *     automatically topologically sorted and files dropped that are not
   *     transitive dependencies of the entry points. Defaults to {@code false}.
   */
  public void setOnlyClosureDependencies(boolean onlyClosureDependencies) {
    this.onlyClosureDependencies = onlyClosureDependencies;
  }

  /**
   * @param outputFile the file to write output to instead of standard output
   */
  public void setOutputFile(String outputFile) {
    this.outputFile = outputFile;
  }

  /**
   * Prints out a list of all the files in the compilation. If
   * {@code manageClosureDependencies} is on, this will not include files
   * that got dropped because they were not required. The {@code %outname%}
   * placeholder expands to the value of {@code outputFile}. If you're using
   * modularization, using {@code %outname%} will create a manifest for each
   * module.
   *
   * @param outputManifest the output manifest file name. May include the
   *     {@code %outname%} placeholder, which expands to the value of
   *     {@code outputFile}.
   */
  public void setOutputManifest(String outputManifest) {
    this.outputManifest = outputManifest;
  }

  /**
   * @param printCommandLine determines if the command line used to run the
   *     Compiler should be printed to standard output. Defaults to
   *     {@code false}.
   */
  public void setPrintCommandLine(boolean printCommandLine) {
    this.printCommandLine = printCommandLine;
  }

  // Nested element setters

  /**
   * Source files for which transitive dependencies will be calculated. Inputs
   * are similar to {@literal <sources>}, except that {@code goog.provided}
   * namespaces are passed to the Compiler as namespace entry points. See
   * {@link #addNamespaceEntryPoint(StringNestedElement)}.
   *
   * <p><b>Note: </b>The {@literal <mainsources>} element is an Ant
   * <a href="http://ant.apache.org/manual/Types/fileset.html">FileSet</a>
   * (i.e. it supports FileSet's attributes and nested elements).</p>
   *
   * @param mainSources a FileSet containing "main sources" (i.e. program
   *     entry points) for which transitive dependencies will be calculated
   */
  public void addInputs(FileSet mainSources) {
    this.mainSources.add(mainSources);
  }

  /**
   * Entry point to the program that must be a Closure namespace. A Closure
   * namespace is a dot-delimited path expression declared with a call to
   * {@code goog.provide()} (for example, "goog.array" or "foo.bar"). Any
   * namespace symbols that are not transitive dependencies of the entry points
   * will be removed. Files without {@code goog.provide()}, and their
   * dependencies, will always be left in.
   *
   * @param namespaceEntryPoint a program entry point; must be a Closure
   *     namespace
   */
  public void addNamespaceEntryPoint(StringNestedElement namespaceEntryPoint) {
    this.namespaceEntryPoints.add(namespaceEntryPoint);
  }

  /**
   * Source files. The {@literal <sourceList>} element is an Ant
   * <a href="http://ant.apache.org/manual/Types/filelist.html">FileList</a>
   * (i.e. it supports FileList's attributes and nested elements).
   *
   * <p><b>Note:</b> sources specified by {@literal <sourceList>} are always
   * passed to the Compiler after {@code inputManifest} and before sources
   * specified by {@literal <sources>}. See
   * {@link #setInputManifest(String)} and {@link #addSources(FileSet)}.
   * </p>
   *
   * @param sourceList a list of source files
   */
  public void addSourceList(FileList sourceList) {
    this.sourceLists.add(sourceList);
  }

  /**
   * Source files. The {@literal <sources>} element is an Ant
   * <a href="http://ant.apache.org/manual/Types/fileset.html">FileSet</a>
   * (i.e. it supports FileSet's attributes and nested elements). If the
   * sources must be in a particular order, use {@literal <sourceList>}
   * instead. See {@link #addSourceList(FileList)}.
   *
   * <p><b>Note:</b> {@literal <sources>} are always passed to the Compiler
   * last after sources specified by {@code inputManifest} and
   * {@literal <sourceList>}. See {@link #setInputManifest(String)} and
   * {@link #addSourceList(FileList)}.</p>
   *
   * @param sources source files
   */
  public void addSources(FileSet sources) {
    this.sources.add(sources);
  }


  // CompilerOptionsBasic

  // Attribute wrappers
  public void setCustomExternsOnly(boolean customExternsOnly) {
    this.compilerOptions.setCustomExternsOnly(customExternsOnly);
  }
  public void setDebug(boolean debug) {
    this.compilerOptions.setDebug(debug);
  }
  public void setLanguageIn(String languageIn) {
    this.compilerOptions.setLanguageIn(languageIn);
  }
  public void setOutputWrapper(String outputWrapper) {
    this.compilerOptions.setOutputWrapper(outputWrapper);
  }
  public void setPrettyPrint(boolean prettyPrint) {
    this.compilerOptions.setPrettyPrint(prettyPrint);
  }
  public void setPrintInputDelimiter(boolean printInputDelimiter) {
    this.compilerOptions.setPrintInputDelimiter(printInputDelimiter);
  }
  public void setPropertyMapInputFile(String inputFile) {
    this.compilerOptions.setPropertyMapInputFile(inputFile);
  }
  public void setPropertyMapOutputFile(String outputFile) {
    this.compilerOptions.setPropertyMapOutputFile(outputFile);
  }
  public void setVariableMapInputFile(String inputFile) {
    this.compilerOptions.setVariableMapInputFile(inputFile);
  }
  public void setVariableMapOutputFile(String outputFile) {
    this.compilerOptions.setVariableMapOutputFile(outputFile);
  }
  public void setWarningLevel(String warningLevel) {
    this.compilerOptions.setWarningLevel(warningLevel);
  }

  // Nested element wrappers
  public void addConfiguredCompilerCheck(NameValuePair compilerCheck){
    this.compilerOptions.addConfiguredCompilerCheck(compilerCheck);
  }
  public void addConfiguredDefine(Parameter define) {
    this.compilerOptions.addConfiguredDefine(define);
  }
  public void addExterns(FileSet externs) {
    this.compilerOptions.addExterns(externs);
  }
  public CommandLineBuilder getCommandLineFlags(Project project) {
    return this.compilerOptions.getCommandLineFlags(project);
  }
  public Set<String> getExterns(Project project) {
    return this.compilerOptions.getExterns(project);
  }
  
  // CompilerOptionsComplete

  // Attribute wrappers
  public void setAcceptConstKeyword(boolean acceptConstKeyword) {
    this.compilerOptions.setAcceptConstKeyword(acceptConstKeyword);
  }
  public void setCharset(String charset) {
    this.compilerOptions.setCharset(charset);
  }
  public void setCommonJsModulePathPrefix(String commonJsModulePathPrefix) {
    this.compilerOptions.setCommonJsModulePathPrefix(commonJsModulePathPrefix);
  }
  public void setCommonJsEntryModule(String commonJsEntryModule) {
    this.compilerOptions.setCommonJsEntryModule(commonJsEntryModule);
  }
  public void setCompilationLevel(String compilationLevel) {
    this.compilerOptions.setCompilationLevel(compilationLevel);
  }
  public CompilationLevel getCompilationLevel() {
    return this.compilerOptions.getCompilationLevel();
  }
  public void setCreateNameMapFiles(boolean createNameMapFiles) {
    this.compilerOptions.setCreateNameMapFiles(createNameMapFiles);
  }
  public void setFlagFile(File flagFile) {
    this.compilerOptions.setFlagFile(flagFile);
  }
  public void setGenerateExports(boolean generateExports) {
    this.compilerOptions.setGenerateExports(generateExports);
  }
  public void setLoggingLevel(String loggingLevel) {
    this.compilerOptions.setLoggingLevel(loggingLevel);
  }
  public void setModuleOutputPathPrefix(String moduleOutputPathPrefix) {
    this.compilerOptions.setModuleOutputPathPrefix(moduleOutputPathPrefix);
  }
  public void setPrintAST(boolean printAST) {
    this.compilerOptions.setPrintAST(printAST);
  }
  public void setPrintPassGraph(boolean printPassGraph) {
    this.compilerOptions.setPrintPassGraph(printPassGraph);
  }
  public void setPrintTree(boolean printTree) {
    this.compilerOptions.setPrintTree(printTree);
  }
  public void setProcessClosurePrimitives(boolean processClosurePrimitives) {
    this.compilerOptions.setProcessClosurePrimitives(processClosurePrimitives);
  }
  public void setProcessCommonJsModules(boolean processCommonJsModules) {
    this.compilerOptions.setProcessCommonJsModules(processCommonJsModules);
  }
  public void setProcessjQueryPrimitives(boolean processjQueryPrimitives) {
    this.compilerOptions.setProcessjQueryPrimitives(processjQueryPrimitives);
  }
  public void setSourceMapFormat(String sourceMapFormat) {
    this.compilerOptions.setSourceMapFormat(sourceMapFormat);
  }
  public void setSourceMapOutputFile(String sourceMapOutputFile) {
    this.compilerOptions.setSourceMapOutputFile(sourceMapOutputFile);
  }
  public void setSummaryDetailLevel(int summaryDetailLevel) {
    this.compilerOptions.setSummaryDetailLevel(summaryDetailLevel);
  }
  public void setThirdParty(boolean thirdParty) {
    this.compilerOptions.setThirdParty(thirdParty);
  }
  public void setTransformAMDModules(boolean transformAMDModules) {
    this.compilerOptions.setTransformAMDModules(transformAMDModules);
  }
  public void setTranslationsFile(File translationsFile) {
    this.compilerOptions.setTranslationsFile(translationsFile);
  }
  public void setTranslationsProject(String translationsProject) {
    this.compilerOptions.setTranslationsProject(translationsProject);
  }

  // Nested element wrappers
  public void addConfiguredFlag(NameValuePair flag) {
    this.compilerOptions.addConfiguredFlag(flag);
  }
  public void addModule(StringNestedElement module) {
    this.compilerOptions.addModule(module);
  }
  public void addModuleWrapper(StringNestedElement moduleWrapper) {
    this.compilerOptions.addModuleWrapper(moduleWrapper);
  }


  /**
   * Execute the Closure Compiler task.
   *
   * @throws BuildException on error.
   */
  public void execute() {

    // Verify task preconditions

    if (this.compilerJar == null) {
      String closureCompilerPath =
          SharedAntProperty.CLOSURE_COMPILER_JAR.getValue(getProject());
      if (closureCompilerPath != null) {
        this.compilerJar = new File(closureCompilerPath);
      } else {
        throw new BuildException("\"compilerJar\" is not set. Verify "
            + "that your build file imports \"closure-tools-config.xml\" and "
            + "that the property locations are correct for your machine.");
      }
    }

    // Execute Closure Compiler.

    Java runner = new Java(this);
    runner.setJar(this.compilerJar);
    runner.setFailonerror(true);
    runner.setFork(true);
    runner.setLogError(true);
    runner.setTaskName(getTaskName());

    // Write temporary flag file to pass the compiler flags. This prevents 
    // errors on Windows when the command line would otherwise exceed the 
    // character limit.
    BuildCache cache = new BuildCache(this);
    StringBuilder compilerFlags = new StringBuilder();

    CommandLineBuilder cmdlineFlags = getCommandLineOptionsExcludingSources();
    List<NameValuePair> flags = cmdlineFlags.getFlagsAsListOfNameValuePair();
    for (NameValuePair flagPair : flags) {
      compilerFlags.append(flagPair.getName()).append("=")
          .append(StringUtil.quoteStringIfContainsWhitespace(
              flagPair.getValue()))
          .append(String.format("%n"));
    }

    // Make sure we are not missing any compiler options passed without flags.
    List<String> args = cmdlineFlags.getArgumentsNotPrecededByFlags();
    for (String arg : args) {
      compilerFlags.append(arg).append(" ");
    }

    List<String> currentSources = getAllSources();
    for (String source : currentSources) {
      if (source != null) {
        compilerFlags.append("--js=");
        compilerFlags.append(StringUtil.quoteStringIfContainsWhitespace(source));
        compilerFlags.append(String.format("%n"));
      }
    }
    File tempFlagFile = cache.createTempFile("temp_flag_file.txt");
    try {
      Files.write(compilerFlags.toString(), tempFlagFile, Charsets.UTF_8);
    } catch (IOException e) {
      throw new BuildException(e);
    }
    runner.createArg().setValue("--flagfile");
    runner.createArg().setValue(tempFlagFile.getAbsolutePath());

    boolean skipCompilation = false;

    if (!this.forceRecompile) {
      // Check if the output file is up-to-date.

      String currentCommandLineAndCompilerFlags =
          runner.getCommandLine().toString() + " " + compilerFlags.toString();
      BuildSettings previousBuildSettings = cache.get();
      BuildSettings currentBuildSettings = new BuildSettings(
          currentCommandLineAndCompilerFlags, currentSources);
      // Save current build settings for the comparison with the next build.
      cache.put(currentBuildSettings);

      if (previousBuildSettings != null) {
        if (ClosureBuildUtil.outputFileUpToDate(new File(this.outputFile),
            previousBuildSettings, currentBuildSettings)) {
          skipCompilation = true;
          log("Output file up-to-date. Compilation skipped.");
        }
      }
    }

    if (!skipCompilation) {
      if (this.printCommandLine) {
        log("Compiling with the following command: "
            + runner.getCommandLine().toString(), Project.MSG_INFO);
      } else {
        log("Compiling " + currentSources.size() + " file(s) with "
            + this.compilerOptions.getExterns(getProject()).size()
            + " extern(s)");
      }

      int exitCode = runner.executeJava();
      if (exitCode != 0) {
        throw new BuildException("Error: " + getTaskName()
            + " finished with exit code " + exitCode);
      }
    }
  }

  /**
   * Gathers command line options based on the attributes and nested elements
   * set for this task.
   *
   * @return command line options based on attribute and nested element settings
   */
  private CommandLineBuilder getCommandLineOptionsExcludingSources() {
    CommandLineBuilder cmdline =
        this.compilerOptions.getCommandLineFlags(getProject());

    if (this.manageClosureDependencies != null) {
      cmdline.flagAndArgument("--manage_closure_dependencies",
          this.manageClosureDependencies.toString());
    }
    if (this.onlyClosureDependencies != null) {
      cmdline.flagAndArgument("--only_closure_dependencies",
          this.onlyClosureDependencies.toString());
    }
    if (this.outputFile != null) {
      cmdline.flagAndArgument("--js_output_file",
          new File(this.outputFile).getAbsolutePath());
    }
    if (this.outputManifest != null) {
      cmdline.flagAndArgument("--output_manifest",
          new File(this.outputManifest).getAbsolutePath());
    }
    for (StringNestedElement namespace : this.namespaceEntryPoints) {
      cmdline.flagAndArgument("--closure_entry_point", namespace.getValue());
    }

    // Add namespaces that are goog.provided in the main sources.
    try {
      for (FileSet sources : this.mainSources) {
        for (File source :
            AntUtil.getListOfFilesFromAntFileSet(getProject(), sources)) {
          List<String> namespaces =
              ClosureBuildUtil.extractGoogProvidedNamespaces(source);

          for (String namespace : namespaces) {
            cmdline.flagAndArgument("--closure_entry_point", namespace);
          }
        }
      }
    } catch (IOException e) {
      throw new BuildException(e);
    }
    return cmdline;
  }

  /**
   * Creates a list of sources based on the {@code inputManifest} file if
   * specified as well as {@literal <mainsources>}, {@literal <sourceList>},
   * and {@literal <sources>}.
   *
   * @return a list of the current build sources (prior to dependency
   *     management)
   * @throws org.apache.tools.ant.BuildException if there is an
   *     {@link java.io.IOException} reading {@code inputManifest}
   */
  private List<String> getAllSources() {
    List<String> currentBuildSources = Lists.newArrayList();

    try {
      if (this.inputManifest != null) {
        currentBuildSources.addAll(Files.readLines(
            new File(this.inputManifest), Charsets.UTF_8));
      }
    } catch (IOException e) {
      throw new BuildException(e);
    }

    currentBuildSources.addAll(
        AntUtil.getFilePathsFromCollectionOfFileSet(getProject(), mainSources));
    for (FileList fileList : this.sourceLists) {
      File baseDir = fileList.getDir(getProject());
      String[] files = fileList.getFiles(getProject());

      for (String file : files) {
        currentBuildSources.add(new File(baseDir, file).getAbsolutePath());
      }
    }
    currentBuildSources.addAll(
        AntUtil.getFilePathsFromCollectionOfFileSet(getProject(), sources));

    return currentBuildSources;
  }
}
