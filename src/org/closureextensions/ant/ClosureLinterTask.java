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

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.FileSet;

import org.closureextensions.ant.types.ClosureLinterErrors;
import org.closureextensions.ant.types.Directory;
import org.closureextensions.common.deps.DirectoryPathPrefixPair;
import org.closureextensions.common.deps.FilePathDepsPathPair;
import org.closureextensions.common.util.StringUtil;

/**
 * Closure Linter Ant task. This task is a wrapper for the Closure Linter
 * Python programs <a target="_blank" href=
 * "http://code.google.com/p/closure-linter/source/browse/trunk/closure_linter/gjslint.py">
 * gjslint.py</a> and <a target="_blank" href=
 * "http://code.google.com/p/closure-linter/source/browse/trunk/closure_linter/fixjsstyle.py">
 * fixjsstyle.py</a>.
 *
 * <p>If {@code gjslint} and {@code fixjsstyle} are defined on your PATH,
 * then the Python script locations do not need to be specified when using
 * the Closure Linter Ant task.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ClosureLinterTask extends Task {

  /** Execution modes supported by the Closure Linter task. Defaults to LINT. */
  public static enum ClosureLinterMode {
    /** Automatically fix simple style guide violations. */
    FIX,

    /** Checks JavaScript files for common style guide violations. */
    LINT,
    ;
  }

  // Note: the Closure Linter command line flags are defined in the following
  // source files: checker.py, checkerbase.py, ecmalintrules.py,
  // error_fixer.py, error_check.py, errorrules.py, fixjsstyle.py, gjslint.py,
  // indentation.py, common/simplefileflags.py.

  // Attributes

  // Corresponds to flag --beep defined in gjslint.py.
  private Boolean beep;

  // Corresponds to flag --check_html defined in gjslint.py.
  private Boolean checkJavaScriptInHtmlFiles;

  // Corresponds to flag --debug_indentation defined in indentation.py.
  private Boolean debugIndentation;

  // Corresponds to flag --debug_tokens defined in checkerbase.py.
  private Boolean debugTokens;

  // Corresponds to flag --disable_indentation_fixing defined in error_fixer.py.
  private Boolean disableIndentationFixing;

  // Corresponds to flag --error_trace defined in checkerbase.py.
  private Boolean errorTrace;

  private File fixjsstylePythonScript;
  private File gjslintPythonScript;
  private ClosureLinterMode linterMode;
  private File logFile;

  // Corresponds to flag --multiprocess defined in gjslint.py.
  private Boolean multiProcess;

  private String pythonExecutable;

  // Corresponds to flag --summary defined in gjslint.py.
  private Boolean showSummary;

  // Corresponds to flag --time defined in gjslint.py.
  private Boolean timingStats;

  // Corresponds to flag --unix_mode defined in gjslint.py.
  private Boolean unixMode;


  // Nested elements

  // Corresponds to flag --additional_extensions defined in gjslint.py.
  private final List<String> additionalJavaScriptExtensions;

  private ClosureLinterErrors closureLinterErrors;

  // Corresponds to flag --custom_jsdoc_tags defined in ecmalintrules.py.
  private final List<String> customJsDocTags;

  //

  // Corresponds to flag --ignored_extra_namespaces defined in checker.py.
  private final List<String> extraNamespacesToIgnore;

  // Serves same purpose as flag --closurized_namespaces defined in checker.py.
  private final List<FileSet> mainSources; // Program entry points

  // Corresponds to flag --closurized_namespaces defined in checker.py.
  private final List<String> namespaces;

  // Serves sames purpose as flag --recurse defined in simplefileflags.py.
  private final List<Directory> roots;

  // Serves same purpose as passing JavaScript source files as extra arguments
  // to gjslint or fixjsstyle
  private final List<FileSet> sources;

  // Corresponds to flag --limited_doc_files defined in checker.py.
  private final List<FileSet> sourcesWithRelaxedDocumentationChecks;


  /**
   * Constructs a new Ant task for Deps Writer.
   */
  public ClosureLinterTask() {
    // Attributes
    this.beep = null;
    this.checkJavaScriptInHtmlFiles = null;
    this.debugIndentation = null;
    this.debugTokens = null;
    this.disableIndentationFixing = null;
    this.errorTrace = null;
    this.fixjsstylePythonScript = null;
    this.gjslintPythonScript = null;
    this.linterMode = ClosureLinterMode.LINT;
    this.logFile = null;
    this.multiProcess = null;
    this.pythonExecutable = "python";
    this.showSummary = null;
    this.timingStats = null;
    this.unixMode = null;

    // Nested elements
    this.additionalJavaScriptExtensions = Lists.newArrayList();
    this.closureLinterErrors = null;
    this.customJsDocTags = Lists.newArrayList();
    this.extraNamespacesToIgnore = Lists.newArrayList();
    this.mainSources = Lists.newArrayList();
    this.namespaces = Lists.newArrayList();
    this.roots = Lists.newArrayList();
    this.sources = Lists.newArrayList();
    this.sourcesWithRelaxedDocumentationChecks = Lists.newArrayList();
  }


  // Attribute setters

  /**
   * Sets the fixjsstyle.py Python script file. Setting this attribute is not
   * necessary if {@code fixjsstyle} is defined on your PATH.
   *
   * @param file the fixjsstyle Python script
   */
  public void setFixjsstylePythonScript(File file) {
    this.fixjsstylePythonScript = file;
  }

  /**
   * Sets the gjslint.py Python script file. Setting this attribute is not
   * necessary if {@code gjslint} is defined on your PATH.
   *
   * @param file the gjslint Python script
   */
  public void setGjslintPythonScript(File file) {
    this.gjslintPythonScript = file;
  }

  /**
   * Sets a log file to store output from {@code gjslint} and {@code
   * fixjsstyle}.
   *
   * @param logFile the log file
   */
  public void setLogFile(File logFile) {
    this.logFile = logFile;
  }

  /**
   * Sets the Python interpreter executable.
   *
   * @param python the Python executable. Defaults to "python".
   */
  public void setPythonExecutable(String python) {
    this.pythonExecutable = python;
  }


  // Nested element setters



  /**
   * Execute the Deps Writer task.
   *
   * @throws org.apache.tools.ant.BuildException on error.
   */
  public void execute() {

    // Verify task preconditions

    if (this.fixjsstylePythonScript == null) {
      String fixjsstylePath =
          SharedAntProperty.FIX_JS_STYLE_PY.getValue(getProject());
      if (fixjsstylePath != null) {
        this.fixjsstylePythonScript = new File(fixjsstylePath);
      } else {
        throw new BuildException("\"fixjsstylePythonScript\" is "
            + "not set. Verify that your PATH includes fixjsstyle.");
      }
    }

    // Build the command line.

    CommandLineBuilder cmdline = new CommandLineBuilder();
    cmdline.argument(this.pythonExecutable);
    cmdline.argument(this.gjslintPythonScript);

    /*for (FilePathDepsPathPair pair : this.paths) {
      if (pair.getFilePath() == null) {
        throw new BuildException("null file path");
      }
      if (pair.getDepsPath() != null) {
        cmdline.flagAndArgument("--path_with_depspath",
            "'" + pair.getFilePath() + "' '" + pair.getDepsPath() + "'");
      } else {
        cmdline.argument(pair.getFilePath());
      }
    }

    for (DirectoryPathPrefixPair dirPrefixPair : roots) {
      if (dirPrefixPair.getDirPath() == null) {
        throw new BuildException("null root directory path");
      }
      if (dirPrefixPair.getPrefix() != null) {
        cmdline.flagAndArgument("--root_with_prefix",
            StringUtil.quoteStringIfContainsWhitespace(
                dirPrefixPair.getDirPath()) + " " +
            StringUtil.quoteStringIfContainsWhitespace(
                dirPrefixPair.getPrefix()));
      } else {
        cmdline.flagAndArgument("--root",
            StringUtil.quoteStringIfContainsWhitespace(
                dirPrefixPair.getDirPath()));
      }
    }

    LogStreamHandler logStreamHandler;
    logStreamHandler = new LogStreamHandler(this, Project.MSG_INFO,
        Project.MSG_WARN);
    Execute runner = new Execute(logStreamHandler);
    runner.setVMLauncher(false);
    runner.setAntRun(getProject());
    runner.setCommandline(cmdline.toStringArray());
    executeDepsWriter(runner);*/
  }

  /**
   * Execute depswriter.py.
   *
   * @param runner the {@link org.apache.tools.ant.taskdefs.Execute} runner
   * @return the exit code returned by depswriter.py
   * @throws org.apache.tools.ant.BuildException if there is an {@link java.io.IOException}
   */
  private int executeDepsWriter(Execute runner) {
    int exitCode = 0;
    /*try {
      exitCode = runner.execute();
      if (exitCode != 0) {
        log("Error: " + this.pythonExecutable + " "
            + this.depsWriterPythonScript + " finished with exit code "
            + exitCode);
      }
    } catch (IOException e) {
      throw new BuildException(e);
    }*/
    return exitCode;
  }
}
