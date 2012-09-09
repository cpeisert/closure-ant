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

package org.closureant.jscomp;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Parameter;

import org.closureant.base.CommandLineBuilder;
import org.closureant.plovr.ExperimentalCompilerOptions;
import org.closureant.plovr.PlovrCompilerOptions;
import org.closureant.plovr.Config;
import org.closureant.plovr.IdGenerator;
import org.closureant.types.NameValuePair;
import org.closureant.types.StringNestedElement;
import org.closureant.util.AntUtil;
import org.closureant.util.StringUtil;

/**
 * Static factory class to create new instances of {@link CompilerOptionsBasic},
 * {@link CompilerOptionsComplete}, and {@link org.closureant.plovr.PlovrCompilerOptions}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class CompilerOptionsFactory {
  private CompilerOptionsFactory() {}

  /**
   * Create a new {@link CompilerOptionsBasic} instance.
   *
   * @return a new {@link CompilerOptionsBasic} instance
   */
  public static CompilerOptionsBasic newCompilerOptionsBasic() {
    return new CompilerOptionsBasicImplementation();
  }

  /**
   * Create a new {@link CompilerOptionsComplete} instance.
   *
   * @return a new {@link CompilerOptionsComplete} instance
   */
  public static CompilerOptionsComplete newCompilerOptionsComplete() {
    return new CompilerOptionsCompleteImplementation();
  }

  /**
   * Create a new {@link org.closureant.plovr.PlovrCompilerOptions} instance.
   *
   * @return a new {@link org.closureant.plovr.PlovrCompilerOptions} instance
   */
  public static PlovrCompilerOptions newCompilerOptionsForPlovr() {
    return new PlovrCompilerOptionsImplementation();
  }


  //----------------------------------------------------------------------------


  // NOTE: the nested compiler-options implementation classes would normally
  // be package private. However, they must be public in order to work with
  // Ant's reflection mechanism.


  // NOTE: Primitive class wrappers are used instead of primitives in
  // order to track whether or not attributes are set in the Ant build file.
  // This enables excluding attributes unless they are explicitly set by the
  // user.

  /**
   * Implementation of {@link CompilerOptionsBasic}.
   */
  public static class CompilerOptionsBasicImplementation
      implements CompilerOptionsBasic {

    // Attributes

    protected Boolean customExternsOnly;
    protected Boolean debug;
    protected String languageIn;
    protected String outputWrapper;
    protected Boolean prettyPrint;
    protected Boolean printInputDelimiter;
    protected String propertyMapInputFile;
    protected String propertyMapOutputFile;
    protected String variableMapInputFile;
    protected String variableMapOutputFile;
    protected String warningLevel;


    // Nested elements

    protected final List<NameValuePair> compilerChecks;
    protected final CompileTimeDefines defines;
    protected final List<FileSet> externs;

    /**
     * Constructs a new instance; should not be called directly. Use
     * {@link CompilerOptionsFactory#newCompilerOptionsBasic()} instead.
     */
    public CompilerOptionsBasicImplementation() {

      // Attributes
      this.customExternsOnly = null;
      this.debug = null;
      this.languageIn = null;
      this.outputWrapper = null;
      this.prettyPrint = null;
      this.printInputDelimiter = null;
      this.propertyMapInputFile = null;
      this.propertyMapOutputFile = null;
      this.variableMapInputFile = null;
      this.variableMapOutputFile = null;
      this.warningLevel = null;

      // Nested elements
      this.compilerChecks = Lists.newArrayList();
      this.defines = new CompileTimeDefines();
      this.externs = Lists.newArrayList();
    }

    // Attribute setters

    public void setCustomExternsOnly(boolean customExternsOnly) {
      this.customExternsOnly = customExternsOnly;
    }

    public void setDebug(boolean debug) {
      this.debug = debug;
    }

    /**
     * @param languageIn the language specification that input sources conform.
     *     Options: "ECMASCRIPT3", "ECMASCRIPT5", "ECMASCRIPT5_STRICT". Defaults
     *     to "ECMASCRIPT3".
     * @throws BuildException if {@code languageIn} is not a valid option
     */
    public void setLanguageIn(String languageIn) {
      if ("ECMASCRIPT3".equalsIgnoreCase(languageIn)
          || "ECMASCRIPT5".equalsIgnoreCase(languageIn)
          || "ECMASCRIPT5_STRICT".equalsIgnoreCase(languageIn)) {

        this.languageIn = languageIn.toUpperCase();
      } else {
        throw new BuildException("languageIn expected to be one of "
            + " \"ECMASCRIPT3\", \"ECMASCRIPT5\", or \"ECMASCRIPT5_STRICT\", "
            + "but was \"" + languageIn.toUpperCase() + "\"");
      }
    }

    /**
     * @param outputWrapper a template into which compiled JavaScript will be
     *     written; the placeholder for compiled code is {@code %output%}
     */
    public void setOutputWrapper(String outputWrapper) {
      this.outputWrapper = outputWrapper;
    }

    public void setPrettyPrint(boolean prettyPrint) {
      this.prettyPrint = prettyPrint;
    }

    public void setPrintInputDelimiter(boolean printInputDelimiter) {
      this.printInputDelimiter = printInputDelimiter;
    }

    public void setPropertyMapInputFile(String inputFile) {
      this.propertyMapInputFile = inputFile;
    }

    public void setPropertyMapOutputFile(String outputFile) {
      this.propertyMapOutputFile = outputFile;
    }

    public void setVariableMapInputFile(String inputFile) {
      this.variableMapInputFile = inputFile;
    }

    public void setVariableMapOutputFile(String outputFile) {
      this.variableMapOutputFile = outputFile;
    }

    /**
     * @param warningLevel the warning level, which must be one of "QUIET",
     *     "DEFAULT", or "VERBOSE". Defaults to "DEFAULT".
     * @throws BuildException if {@code warningLevel} is not a valid option
     */
    public void setWarningLevel(String warningLevel) {
      String warningLevelUpperCase = warningLevel.toUpperCase();

      if ("QUIET".equals(warningLevelUpperCase)
          || "DEFAULT".equals(warningLevelUpperCase)
          || "VERBOSE".equals(warningLevelUpperCase)) {
        this.warningLevel = warningLevelUpperCase;
      } else {
        throw new BuildException("warningLevel expected to be one of "
            + " \"QUIET\", \"DEFAULT\", or \"VERBOSE\", "
            + "but was \"" + warningLevelUpperCase + "\"");
      }
    }


    // Nested element setters

    /**
     * @param compilerCheck a name-value pair where the name is a Closure
     *     Compiler check and the value is a check level. The supported checks
     *     correspond to the diagnostic groups defined in
     *     <a href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/DiagnosticGroups.java">
     *     DiagnosticGroups.java</a>. The check
     *     level must be one of "OFF", "WARNING", or "ERROR".
     * @throws BuildException if the check level is not a valid option
     */
    public void addConfiguredCompilerCheck(NameValuePair compilerCheck) {
      if (compilerCheck == null) {
        throw new BuildException("compilerCheck was null");
      }

      String checkLevelUpperCase = compilerCheck.getValue().toUpperCase();

      if ("OFF".equals(checkLevelUpperCase)
          || "WARNING".equals(checkLevelUpperCase)
          || "ERROR".equals(checkLevelUpperCase)) {

        this.compilerChecks.add(compilerCheck);
      } else {
        throw new BuildException("compiler check level expected to be one of "
            + " \"OFF\", \"WARNING\", or \"ERROR\", "
            + "but was \"" + checkLevelUpperCase + "\"");
      }
    }

    /**
     * For more information about compile-time defines, see: <a target="_blank"
     * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
     * Definitive Guide</i></a> page 353.
     *
     * @param define a compile-time override for a JavaScript variable
     *     annotated with {@code @define}. The {@code type} field must be set
     *     to one of "boolean", "number", or "string". If the type is "string",
     *     then the {@code value} field will be automatically quoted.
     * @throws BuildException if the {@code name}, {@code value},
     *     or {@code type} field is {@code null}, or if the {@code type} field
     *     is not one of "boolean", "number", or "string"
     */
    public void addConfiguredDefine(Parameter define) {
      if (define == null) {
        throw new BuildException("define was null");
      }
      if (define.getName() == null) {
        throw new BuildException("<define> \"name\" attribute not set");
      }
      if (define.getValue() == null) {
        throw new BuildException("<define> \"value\" attribute not set");
      }
      if (define.getType() == null) {
        throw new BuildException("<define> \"type\" attribute not set; must "
            + "be one of \"boolean\", \"number\", or \"string\"");
      }

      this.defines.put(define);
    }

    public void addExterns(FileSet externs) {
      this.externs.add(externs);
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
    public CommandLineBuilder getCommandLineFlags(Project project) {
      CommandLineBuilder cmdline = new CommandLineBuilder();

      // Attributes

      if (customExternsOnly != null) {
        cmdline.flagAndArgument("--use_only_custom_externs",
            customExternsOnly.toString());
      }
      if (debug != null) {
        cmdline.flagAndArgument("--debug", debug.toString());
      }
      if (languageIn != null) {
        cmdline.flagAndArgument("--language_in", languageIn);
      }
      if (outputWrapper != null) {
        cmdline.flagAndArgument("--output_wrapper", outputWrapper);
      }
      if (Boolean.TRUE.equals(prettyPrint)) {
        cmdline.flagAndArgument("--formatting", "PRETTY_PRINT");
      }
      if (Boolean.TRUE.equals(printInputDelimiter)) {
        cmdline.flagAndArgument("--formatting", "PRINT_INPUT_DELIMITER");
      }
      if (propertyMapInputFile != null) {
        cmdline.flagAndArgument("--property_map_input_file",
            propertyMapInputFile);
      }
      if (propertyMapOutputFile != null) {
        cmdline.flagAndArgument("--property_map_output_file",
            propertyMapOutputFile);
      }
      if (variableMapInputFile != null) {
        cmdline.flagAndArgument("--variable_map_input_file",
            variableMapInputFile);
      }
      if (variableMapOutputFile != null) {
        cmdline.flagAndArgument("--variable_map_output_file",
            variableMapOutputFile);
      }
      if (warningLevel != null) {
        cmdline.flagAndArgument("--warning_level", warningLevel);
      }


      // Nested elements

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
        cmdline.flagAndFileSet("--externs", externFiles, project);
      }

      return cmdline;
    }

    /**
     * Get the set of extern files.
     *
     * @param project the Ant project
     * @return a {@link Set} of extern files or an empty set if no externs
     *     specified
     */
    public Set<String> getExterns(Project project) {
      Set<String> externFiles = Sets.newHashSet();

      externFiles.addAll(
          AntUtil.getFilePathsFromCollectionOfFileSet(project, externs));
      return externFiles;
    }
  }


  //----------------------------------------------------------------------------


  /**
   * Implementation of {@link CompilerOptionsComplete}.
   */
  public static class CompilerOptionsCompleteImplementation
      extends CompilerOptionsBasicImplementation
      implements CompilerOptionsComplete {

    private final List<String> flagFileArgs;

    // Attributes

    protected Boolean acceptConstKeyword;
    protected String charset;
    protected String commonJsModulePathPrefix;
    protected String commonJsEntryModule;
    protected CompilationLevel compilationLevel;
    protected Boolean createNameMapFiles;
    protected File flagFile;
    protected Boolean generateExports;
    protected Level loggingLevel;
    protected String moduleOutputPathPrefix;
    protected Boolean printAST;
    protected Boolean printCommandLine;
    protected Boolean printPassGraph;
    protected Boolean printTree;
    protected Boolean processClosurePrimitives;
    protected Boolean processCommonJsModules;
    protected Boolean processjQueryPrimitives;
    protected String sourceMapFormat;
    protected String sourceMapOutputFile;
    protected Integer summaryDetailLevel;
    protected Boolean transformAMDModules;
    protected Boolean thirdParty;
    protected File translationsFile;
    protected String translationsProject;

    // Nested elements

    protected final CommandLineBuilder flags;
    protected final List<StringNestedElement> modules;
    protected final List<StringNestedElement> moduleWrappers;


    /**
     * Constructs a new instance; should not be called directly. Use
     * {@link CompilerOptionsFactory#newCompilerOptionsComplete()} instead.
     */
    public CompilerOptionsCompleteImplementation() {
      super();

      this.flagFileArgs = Lists.newArrayList();

      // Attributes

      this.acceptConstKeyword = null;
      this.charset = null;
      this.commonJsModulePathPrefix = null;
      this.commonJsEntryModule = null;
      this.compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;
      this.createNameMapFiles = null;
      this.flagFile = null;
      this.generateExports = null;
      this.loggingLevel = null;
      this.moduleOutputPathPrefix = null;
      this.printAST = null;
      this.printCommandLine = null;
      this.printPassGraph = null;
      this.printTree = null;
      this.processClosurePrimitives = null;
      this.processCommonJsModules = null;
      this.processjQueryPrimitives = null;
      this.sourceMapFormat = null;
      this.sourceMapOutputFile = null;
      this.summaryDetailLevel = null;
      this.thirdParty = null;
      this.transformAMDModules = null;
      this.translationsFile = null;
      this.translationsProject = null;


      // Nested elements

      this.flags = new CommandLineBuilder();
      this.modules = Lists.newArrayList();
      this.moduleWrappers = Lists.newArrayList();
    }


    // Attribute setters

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
     * @param commonJsModulePathPrefix path prefix to be removed from Common JS
     *     module names
     */
    public void setCommonJsModulePathPrefix(String commonJsModulePathPrefix) {
      this.commonJsModulePathPrefix = commonJsModulePathPrefix;
    }

    /**
     * @param commonJsEntryModule root of your common JS dependency hierarchy.
     *     Your main script.
     */
    public void setCommonJsEntryModule(String commonJsEntryModule) {
      this.commonJsEntryModule = commonJsEntryModule;
    }

    /**
     * @param compilationLevel specifies the compilation level to use. Options:
     *     "WHITESPACE_ONLY" (or "WHITESPACE"), "SIMPLE_OPTIMIZATIONS" (or
     *     "SIMPLE"), "ADVANCED_OPTIMIZATIONS" (or "ADVANCED"). Defaults to
     *     "SIMPLE_OPTIMIZATIONS".
     * @throws BuildException if {@code compilationLevel} is not a valid option
     */
    public void setCompilationLevel(String compilationLevel) {
      this.compilationLevel = CompilationLevel.fromString(compilationLevel);
      if (this.compilationLevel == null) {
        throw new BuildException("compilationLevel expected to be one of "
            + "\"WHITESPACE_ONLY\" (or \"WHITESPACE\"), "
            + "\"SIMPLE_OPTIMIZATIONS\" (or \"SIMPLE\"), or "
            + "\"ADVANCED_OPTIMIZATIONS\" (or \"ADVANCED\"), but was \""
            + compilationLevel.toUpperCase() + "\"");
      }
    }

    /**
     * Get the compilation level. See {@link CompilationLevel}.
     *
     * @return the compilation level
     */
    public CompilationLevel getCompilationLevel() {
      return this.compilationLevel;
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
     * @param flagFile a file containing additional command-line options
     */
    public void setFlagFile(File flagFile) {
      this.flagFile = flagFile;
    }

    /**
     * @param generateExports generates export code for those marked with
     *     {@code @export}
     */
    public void setGenerateExports(boolean generateExports) {
      this.generateExports = generateExports;
    }

    /**
     * @param loggingLevel the logging level (standard
     *     {@link java.util.logging.Level} values) for Compiler progress.
     *     Does not control errors or warnings for the JavaScript code under
     *     compilation.
     * @throws NullPointerException if the loggingLevel is {@code null}
     * @throws IllegalArgumentException if the value is not valid. Valid values
     *     are integers between Integer.MIN_VALUE and Integer.MAX_VALUE, and all
     *     known level names.
     * @see java.util.logging.Level
     */
    public void setLoggingLevel(String loggingLevel) {
      this.loggingLevel = Level.parse(loggingLevel);
    }

    /**
     * Prefix for file names of compiled js modules. {@literal {module-name}.js}
     * will be appended to this prefix. Directories will be created as needed.
     * Use with the nested element {@literal <module>}.
     *
     * @param moduleOutputPathPrefix the module output prefix
     */
    public void setModuleOutputPathPrefix(String moduleOutputPathPrefix) {
      this.moduleOutputPathPrefix = moduleOutputPathPrefix;
    }

    /**
     * @param printAST prints a dot file describing the internal abstract syntax
     *     tree and exits
     */
    public void setPrintAST(boolean printAST) {
      this.printAST = printAST;
    }

    /**
     * @param printPassGraph prints a dot file describing the passes that will
     *     get run and exits
     */
    public void setPrintPassGraph(boolean printPassGraph) {
      this.printPassGraph = printPassGraph;
    }

    /**
     * @param printTree prints out the parse tree and exits
     */
    public void setPrintTree(boolean printTree) {
      this.printTree = printTree;
    }

    /**
     * @param processClosurePrimitives processes built-ins from the Closure
     *     library, such as {@code goog.require()}, {@code goog.provide()},
     *     and {@code goog.exportSymbol()}
     */
    public void setProcessClosurePrimitives(boolean processClosurePrimitives) {
      this.processClosurePrimitives = processClosurePrimitives;
    }

    /**
     * @param processCommonJsModules process Common JS modules to a
     *     concatenable form
     */
    public void setProcessCommonJsModules(boolean processCommonJsModules) {
      this.processCommonJsModules = processCommonJsModules;
    }

    /**
     * @param processjQueryPrimitives processes built-ins from the jQuery
     *     library, such as {@code jQuery.fn} and {@code jQuery.extend()}
     */
    public void setProcessjQueryPrimitives(boolean processjQueryPrimitives) {
      this.processjQueryPrimitives = processjQueryPrimitives;
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
     * Controls how detailed the compilation summary is. Values: 0 (never print
     * summary), 1 (print summary only if there are errors or warnings), 2
     * (print summary if type checking is on, see nested element
     * {@literal <compilerCheck>}, 3 (always print summary). The default level
     * is 1.
     *
     * @param summaryDetailLevel the summary detail level
     */
    public void setSummaryDetailLevel(int summaryDetailLevel) {
      this.summaryDetailLevel = summaryDetailLevel;
    }

    public void setTransformAMDModules(boolean transformAMDModules) {
      this.transformAMDModules = transformAMDModules;
    }

    public void setTranslationsFile(File translationsFile) {
      this.translationsFile = translationsFile;
    }

    public void setTranslationsProject(String translationsProject) {
      this.translationsProject = translationsProject;
    }

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
     * A javascript module specification. The format is "{@literal
     * <name>:<num-js-files>[:[<dep>,...][:]]]}". Module names must be unique.
     * Each dep is the name of a module that this module depends on. Modules
     * must be listed in dependency order, and js source files must be listed in
     * the corresponding order.
     *
     * @param module a module specification
     */
    public void addModule(StringNestedElement module) {
      this.modules.add(module);
    }

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
    public void addModuleWrapper(StringNestedElement moduleWrapper) {
      this.moduleWrappers.add(moduleWrapper);
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
    public CommandLineBuilder getCommandLineFlags(Project project) {
      CommandLineBuilder cmdline = super.getCommandLineFlags(project);

      // Attributes

      if (acceptConstKeyword != null) {
        cmdline.flagAndArgument("--accept_const_keyword",
            acceptConstKeyword.toString());
      }
      if (charset != null) {
        cmdline.flagAndArgument("--charset", charset);
      }
      if (this.commonJsModulePathPrefix != null) {
        cmdline.flagAndArgument("--common_js_module_path_prefix",
            this.commonJsModulePathPrefix);
      }
      if (this.commonJsEntryModule != null) {
        cmdline.flagAndArgument("--common_js_entry_module",
            this.commonJsEntryModule);
      }
      cmdline.flagAndArgument("--compilation_level",
          compilationLevel.toString());
      if (createNameMapFiles != null) {
        cmdline.flagAndArgument("--create_name_map_files",
            createNameMapFiles.toString());
      }
      if (this.flagFile != null) {
        // A flag file is used by Closure Compiler task to pass all compiler
        // options to the compiler to avoid exceeding the command line
        // character limit on Windows. An error will result if a flag file
        // includes a reference to another flag file. Hence, we need to read
        // the flag file arguments and add them to our command line builder.
        String flagData;
        try {
          flagData = Files.toString(this.flagFile, Charsets.UTF_8);
        } catch (IOException e) {
          throw new BuildException(e);
        }
        if (flagData != null) {
          List<String> args = StringUtil.tokenizeKeepingQuotedStrings(flagData);
          List<String> processedFileArgs =
              processArgs(args.toArray(new String[] {}));
          for (String arg : processedFileArgs) {
            this.flagFileArgs.add(arg);
          }
        }
      }
      if (this.generateExports != null) {
        cmdline.flagAndArgument("--generate_exports",
            this.generateExports.toString());
      }
      if (this.loggingLevel != null) {
        cmdline.flagAndArgument("--logging_level", this.loggingLevel.toString());
      }
      if (this.moduleOutputPathPrefix != null) {
        cmdline.flagAndArgument("--module_output_path_prefix",
            this.moduleOutputPathPrefix);
      }
      if (this.printAST != null) {
        cmdline.flagAndArgument("--print_ast", this.printAST.toString());
      }
      if (this.printPassGraph != null) {
        cmdline.flagAndArgument("--print_pass_graph",
            this.printPassGraph.toString());
      }
      if (this.printTree != null) {
        cmdline.flagAndArgument("--print_tree", this.printTree);
      }
      if (this.processClosurePrimitives != null) {
        cmdline.flagAndArgument("--process_closure_primitives",
            this.processClosurePrimitives.toString());
      }
      if (this.processCommonJsModules != null) {
        cmdline.flagAndArgument("--process_common_js_modules",
            this.processCommonJsModules.toString());
      }
      if (this.processjQueryPrimitives != null) {
        cmdline.flagAndArgument("--process_jquery_primitives",
            this.processjQueryPrimitives.toString());
      }
      if (sourceMapFormat != null) {
        cmdline.flagAndArgument("--source_map_format", sourceMapFormat);
      }
      if (sourceMapOutputFile != null) {
        cmdline.flagAndArgument("--create_source_map", sourceMapOutputFile);
      }
      if (this.summaryDetailLevel != null) {
        cmdline.flagAndArgument("--summary_detail_level",
            this.summaryDetailLevel.toString());
      }
      if (thirdParty != null) {
        cmdline.flagAndArgument("--third_party", thirdParty.toString());
      }
      if (this.transformAMDModules != null) {
        cmdline.flagAndArgument("--transform_amd_modules",
            this.transformAMDModules.toString());
      }
      if (this.translationsFile != null) {
        cmdline.flagAndArgument("--translations_file",
            this.translationsFile.getAbsolutePath());
      }
      if (this.translationsProject != null) {
        cmdline.flagAndArgument("--translations_project",
            this.translationsProject);
      }

      // Add nested elements to the command line.

      cmdline.commandLineBuilder(this.flags);

      for (StringNestedElement module : this.modules) {
        cmdline.flagAndArgument("--module", module.getValue());
      }
      for (StringNestedElement moduleWrapper : this.moduleWrappers) {
        cmdline.flagAndArgument("--module_wrapper", moduleWrapper.getValue());
      }

      // Remove any command line flags that were also set in the flag file to
      // ensure that the flag file settings take precedence.
      CommandLineBuilder finalCmdline = new CommandLineBuilder();

      if (this.flagFileArgs.isEmpty()) {
        finalCmdline = cmdline;
      } else {
        Set<String> flags = Sets.newHashSet(this.flagFileArgs);
        for (NameValuePair pair : cmdline.getFlagsAsListOfNameValuePair()) {
          if (pair.getName() != null) {
            if (!flags.contains(pair.getName())) {
              finalCmdline.flagAndArgument(pair.getName(), pair.getValue());
            }
          }
        }
        for (String singleArg : cmdline.getArgumentsNotPrecededByFlags()) {
          if (singleArg != null) {
            if (!flags.contains(singleArg)) {
              finalCmdline.argument(singleArg);
            }
          }
        }
        for (String arg : this.flagFileArgs) {
          finalCmdline.argument(arg);
        }
      }

      return finalCmdline;
    }

    // Function from Closure Compiler CommandLineRunner.java
    private static List<String> processArgs(String[] args) {
      // Args4j has a different format that the old command-line parser.
      // So we use some voodoo to get the args into the format that args4j
      // expects.
      Pattern argPattern = Pattern.compile("(--[a-zA-Z_]+)=(.*)");
      Pattern quotesPattern = Pattern.compile("^['\"](.*)['\"]$");
      List<String> processedArgs = Lists.newArrayList();

      for (String arg : args) {
        Matcher matcher = argPattern.matcher(arg);
        if (matcher.matches()) {
          processedArgs.add(matcher.group(1));

          String value = matcher.group(2);
          Matcher quotesMatcher = quotesPattern.matcher(value);
          if (quotesMatcher.matches()) {
            processedArgs.add(quotesMatcher.group(1));
          } else {
            processedArgs.add(value);
          }
        } else {
          processedArgs.add(arg);
        }
      }

      return processedArgs;
    }
  }


  //----------------------------------------------------------------------------


  /**
   * Implementation of {@link CompilerOptionsComplete}.
   */
  public static class PlovrCompilerOptionsImplementation
      extends CompilerOptionsBasicImplementation
      implements PlovrCompilerOptions {

    // Attributes

    protected Boolean ambiguateProperties;
    protected PlovrCompilationMode compilationMode;
    protected Boolean disambiguateProperties;
    protected String outputCharset;
    protected String sourceMapFormat;
    protected String sourceMapOutputFile;
    protected Boolean treatWarningsAsErrors;


    // Nested elements

    protected final ExperimentalCompilerOptions experimentalCompilerOptions;
    protected final List<IdGenerator> idGenerators;
    protected final List<StringNestedElement> nameSuffixesToStrip;
    protected final List<StringNestedElement> outputWrapperNestedElement;
    protected final List<StringNestedElement> typePrefixesToStrip;


    /**
     * Constructs a new instance; should not be called directly. Use
     * {@link CompilerOptionsFactory#newCompilerOptionsForPlovr()} instead.
     */
    public PlovrCompilerOptionsImplementation() {
      super();

      // Attributes
      this.ambiguateProperties = null;
      this.compilationMode = PlovrCompilationMode.SIMPLE;
      this.disambiguateProperties = null;
      this.outputCharset = null;
      this.sourceMapFormat = null;
      this.sourceMapOutputFile = null;
      this.treatWarningsAsErrors = null;

      // Nested elements
      this.experimentalCompilerOptions = new ExperimentalCompilerOptions();
      this.idGenerators = Lists.newArrayList();
      this.outputWrapperNestedElement = Lists.newArrayList();
      this.nameSuffixesToStrip = Lists.newArrayList();
      this.typePrefixesToStrip = Lists.newArrayList();
    }


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
    public void setAmbiguateProperties(boolean ambiguateProperties) {
      this.ambiguateProperties = ambiguateProperties;
    }

    /**
     * @param compilationMode specifies the compilation mode to use. Options:
     *     "RAW", "WHITESPACE" (or "WHITESPACE_ONLY"), "SIMPLE" (or
     *     "SIMPLE_OPTIMIZATIONS"), "ADVANCED" (or "ADVANCED_OPTIMIZATIONS").
     *     Defaults to "SIMPLE".
     * @throws org.apache.tools.ant.BuildException if {@code compilationMode}
     *     is not a valid option
     */
    public void setCompilationMode(String compilationMode) {
      String compilationModeUpperCase = compilationMode.toUpperCase();

      if (compilationModeUpperCase.startsWith("RAW")) {
        this.compilationMode = PlovrCompilationMode.RAW;
      } else if (compilationModeUpperCase.startsWith("WHITESPACE")) {
        this.compilationMode = PlovrCompilationMode.WHITESPACE;
      } else if (compilationModeUpperCase.startsWith("SIMPLE")) {
        this.compilationMode = PlovrCompilationMode.SIMPLE;
      } else if (compilationModeUpperCase.startsWith("ADVANCED")) {
        this.compilationMode = PlovrCompilationMode.ADVANCED;
      } else {
        throw new BuildException("compilationMode expected to be one of "
            + "\"RAW\", \"WHITESPACE\" (or \"WHITESPACE_ONLY\"), \"SIMPLE\" "
            + "(or \"SIMPLE_OPTIMIZATIONS\"), \"ADVANCED\" (or "
            + "\"ADVANCED_OPTIMIZATIONS\") but was \""
            + compilationModeUpperCase + "\"");
      }
    }

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
    public void setDisambiguateProperties(boolean disambiguateProperties) {
      this.disambiguateProperties = disambiguateProperties;
    }

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
    public void setOutputCharset(String outputCharset) {
      this.outputCharset = outputCharset;
    }

    /**
     * @param sourceMapFormat the source map format to produce. Options: "V1",
     *     "V2", "V3", "DEFAULT". Defaults to "DEFAULT", which produces "V2".
     * @throws org.apache.tools.ant.BuildException if {@code sourceMapFormat} is
     *     not a valid option
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
     * @param sourceMapOutputFile file where the mapping from compiled code
     *     back to original source code should be saved. The {@code %outname%}
     *     placeholder will expand to the name of the output file that the
     *     source map corresponds to.
     */
    public void setSourceMapOutputFile(String sourceMapOutputFile) {
      this.sourceMapOutputFile = sourceMapOutputFile;
    }

    /**
     * Getter for the source map output file. This getter exists because as of
     * the February 2012 release of plovr, there is no config file option to
     * specify the source map output path. However, the plovr "build" command
     * does provide a command line flag {@code --create_source_map} to specify
     * where the source map should be written.
     *
     * @return the source map output file or {@code null} if not set
     */
    public String getSourceMapOutputFile() {
      return this.sourceMapOutputFile;
    }

    /**
     * @param treatWarningsAsErrors When set to {@code true}, warnings will be
     *     reported as errors. Recall that compilation will still succeed if
     *     there are warnings, but will fail if there are any errors, so
     *     enabling this option will draw more attention to any potential
     *     problems detected by the Closure Compiler.
     */
    public void setTreatWarningsAsErrors(boolean treatWarningsAsErrors) {
      this.treatWarningsAsErrors = treatWarningsAsErrors;
    }


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
    public void addConfiguredExperimentalCompilerOption(
        Parameter compilerOption) {

      if (compilerOption == null) {
        throw new BuildException("compilerOption was null");
      }
      if (compilerOption.getName() == null) {
        throw new BuildException("<experimentalCompilerOption> attribute "
            + "\"name\" was not set");
      }
      if (compilerOption.getValue() == null) {
        throw new BuildException("<experimentalCompilerOption> attribute "
            + "\"value\" was not set");
      }
      if (compilerOption.getType() == null) {
        throw new BuildException("<experimentalCompilerOption> \"type\" "
            + "attribute not set; must be one of \"boolean\", \"number\", or "
            + "\"string\"");
      }

      this.experimentalCompilerOptions.put(compilerOption);
    }

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
    public void addIdGenerator(IdGenerator idGenerator) {
      this.idGenerators.add(idGenerator);
    }

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
    public void addNameSuffixToStrip(StringNestedElement nameSuffix) {
      this.nameSuffixesToStrip.add(nameSuffix);
    }

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
    public void addOutputWrapper(StringNestedElement outputWrapper) {
      this.outputWrapperNestedElement.add(outputWrapper);
    }

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
    public void addTypePrefixToStrip(StringNestedElement typePrefix) {
      this.typePrefixesToStrip.add(typePrefix);
    }

    /**
     * Creates a {@link org.closureant.plovr.Config} object with the
     * compiler options set based on the field values of this object.
     *
     * @param project the Ant project
     * @return a plovr config file object
     * @throws org.apache.tools.ant.BuildException on error
     */
    public Config toPlovrConfig(Project project) {
      Config config = new Config();

      // Attributes inherited from CompilerOptionsBasicImplementation

      if (this.customExternsOnly != null) {
        config.customExternsOnly = this.customExternsOnly;
      }
      if (this.debug != null) {
        config.debug = this.debug;
      }
      if (this.languageIn != null) {
        config.experimentalCompilerOptions.put("languageIn",
            new JsonPrimitive(this.languageIn));
      }
      if (this.outputWrapper != null) {
        config.outputWrapper.add(this.outputWrapper);
      }
      for (StringNestedElement outputWrapperElement :
          this.outputWrapperNestedElement) {
        config.outputWrapper.add(outputWrapperElement.getValue());
      }
      if (this.prettyPrint != null) {
        config.prettyPrint = this.prettyPrint;
      }
      if (this.printInputDelimiter != null) {
        config.printInputDelimiter = this.printInputDelimiter;
      }
      config.propertyMapInputFile =
          Strings.emptyToNull(this.propertyMapInputFile);
      config.propertyMapOutputFile =
          Strings.emptyToNull(this.propertyMapOutputFile);
      config.variableMapInputFile =
          Strings.emptyToNull(this.variableMapInputFile);
      config.variableMapOutputFile =
          Strings.emptyToNull(this.variableMapOutputFile);
      config.level = Strings.emptyToNull(this.warningLevel);


      // Attributes implemented in PlovrClosureCompiler

      if (this.ambiguateProperties != null) {
        config.ambiguateProperties = this.ambiguateProperties;
      }
      config.mode = this.compilationMode.toString();
      if (this.disambiguateProperties != null) {
        config.disambiguateProperties = this.disambiguateProperties;
      }
      config.outputCharset = this.outputCharset;
      if (this.sourceMapFormat != null) {
        config.experimentalCompilerOptions.put("sourceMapFormat",
            new JsonPrimitive(this.sourceMapFormat));
      }
      /*
      // Setting the sourceMapOutputPath using plovr's experimental compiler
      // options has no effect. Need to use plovr's command line option in
      // "build" mode --create_source_map
      if (this.sourceMapOutputFile != null) {
        config.experimentalCompilerOptions.put("sourceMapOutputPath",
            new JsonPrimitive(
                new File(this.sourceMapOutputFile).getAbsolutePath()));
      }
      */
      if (this.treatWarningsAsErrors != null) {
        config.treatWarningsAsErrors = this.treatWarningsAsErrors;
      }


      // Nested elements inherited from AbstractClosureCompiler

      for (NameValuePair pair : compilerChecks) {
        String checkLevelUpperCase = pair.getValue().toUpperCase();
        if ("OFF".equals(checkLevelUpperCase)
            || "WARNING".equals(checkLevelUpperCase)
            || "ERROR".equals(checkLevelUpperCase)) {
          config.checks.put(pair.getName(), checkLevelUpperCase);
        } else {
          throw new BuildException("compiler check level expected to be one "
              + "of \"OFF\", \"WARNING\", or \"ERROR\" but was \""
              + checkLevelUpperCase + "\"");
        }
      }

      for (Map.Entry<String, JsonPrimitive> define : this.defines.entrySet()) {
        config.define.put(define.getKey(), define.getValue());
      }

      for (FileSet externFiles : externs) {
        List<File> listOfExterns =
            AntUtil.getListOfFilesFromAntFileSet(project, externFiles);
        for (File extern : listOfExterns) {
          config.externs.add(extern.getAbsolutePath());
        }
      }


      // Nested elements implemented in PlovrClosureCompiler

      for (Map.Entry<String, JsonPrimitive> compilerOption :
          this.experimentalCompilerOptions.entrySet()) {
        config.experimentalCompilerOptions.put(compilerOption.getKey(),
            compilerOption.getValue());
      }

      for (IdGenerator idGenerator : this.idGenerators) {
        config.idGenerators.add(idGenerator.getIdGenerator());
      }

      for (StringNestedElement nameSuffixToStrip : this.nameSuffixesToStrip) {
        config.nameSuffixesToStrip.add(nameSuffixToStrip.getValue());
      }

      // Note: outputWrapperNestedElement already handled above with
      // {@code this.outputWrapper}

      for (StringNestedElement typePrefixToStrip : this.typePrefixesToStrip) {
        config.typePrefixesToStrip.add(typePrefixToStrip.getValue());
      }

      return config;
    }
  }
}