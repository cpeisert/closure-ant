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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.FileSet;

import org.closureextensions.ant.types.ClosureLinterErrors;
import org.closureextensions.ant.types.DocTagList;
import org.closureextensions.ant.types.FileExtensionList;
import org.closureextensions.ant.types.NamespaceList;
import org.closureextensions.ant.types.RestrictedDirSet;
import org.closureextensions.common.JsClosureSourceFile;
import org.closureextensions.common.SourceFileFactory;
import org.closureextensions.common.util.FileUtil;


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
 * the Closure Linter Ant task. To execute the Python scripts directly,
 * it may be necessary to explicitly set the Python executable. See {@link
 * #setPythonExecutable(String)}.</p>
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

  private final static String FIXJSSTYLE = "fixjsstyle";
  private final static String GJSLINT = "gjslint";

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

  private String fixjsstylePythonScript;
  private boolean force;
  private String gjslintPythonScript;
  private ClosureLinterMode linterMode;
  private File logFile;

  // Corresponds to flag --multiprocess defined in gjslint.py.
  private Boolean multiProcess;

  private String pythonExecutable;
  private boolean showCommandLine;

  // Corresponds to flag --summary defined in gjslint.py.
  private Boolean showSummary;

  // Corresponds to flag --time defined in gjslint.py.
  private Boolean timingStats;

  // Corresponds to flag --unix_mode defined in gjslint.py.
  private Boolean unixMode;


  // Nested elements

  // Corresponds to flag --additional_extensions defined in gjslint.py.
  private final List<String> additionalJSFileExtensions;

  private ClosureLinterErrors closureLinterErrors;

  // Corresponds to flag --custom_jsdoc_tags defined in ecmalintrules.py.
  private final List<String> customJsDocTags;

  // Corresponds to flag --ignored_extra_namespaces defined in checker.py.
  private final List<String> extraNamespacesToIgnore;

  // Serves same purpose as flag --closurized_namespaces defined in checker.py.
  private final List<FileSet> mainSources; // Program entry points

  // Corresponds to flag --closurized_namespaces defined in checker.py.
  private final List<String> namespaces;

  // Serves sames purpose as flag --recurse defined in simplefileflags.py.
  private final List<RestrictedDirSet> roots;

  // Serves same purpose as passing JavaScript source files as extra arguments
  // to gjslint or fixjsstyle
  private final List<FileSet> sources;

  // Corresponds to flag --limited_doc_files defined in checker.py.
  private final List<FileSet> sourcesWithRelaxedDocumentationChecks;


  /**
   * Constructs a new Closure Linter Ant task.
   */
  public ClosureLinterTask() {
    // Attributes
    this.beep = null;
    this.checkJavaScriptInHtmlFiles = null;
    this.debugIndentation = null;
    this.debugTokens = null;
    this.disableIndentationFixing = null;
    this.errorTrace = null;
    this.fixjsstylePythonScript = FIXJSSTYLE;
    this.force = false;
    this.gjslintPythonScript = GJSLINT;
    this.linterMode = ClosureLinterMode.LINT;
    this.logFile = null;
    this.multiProcess = null;
    this.pythonExecutable = null;
    this.showCommandLine = false;
    this.showSummary = null;
    this.timingStats = null;
    this.unixMode = null;

    // Nested elements
    this.additionalJSFileExtensions = Lists.newArrayList();
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
   * Sets whether an audible beep should be emitted when errors are found.
   *
   * @param beep {@code true} to enable beeps. Defaults to {@code true}.
   */
  public void setBeep(boolean beep) {
    this.beep = beep;
  }

  /**
   * Sets whether to check JavaScript in HTML files.
   *
   * @param checkJSInHtmlFiles {@code true} to check HTML in JavaScript files.
   *     Defaults to {@code false}.
   */
  public void setCheckJSInHtmlFiles(boolean checkJSInHtmlFiles) {
    this.checkJavaScriptInHtmlFiles = checkJSInHtmlFiles;
  }

  /**
   * Whether to print debugging information for indentation.
   *
   * @param debugIndentation {@code true} to print debugging information for
   *     indentation. Defaults to {@code false}.
   */
  public void setDebugIndentation(boolean debugIndentation) {
    this.debugIndentation = debugIndentation;
  }

  /**
   * Whether to print all tokens for debugging.
   *
   * @param debugTokens {@code true} to print all tokens for debugging.
   *     Defaults to {@code false}.
   */
  public void setDebugTokens(boolean debugTokens) {
    this.debugTokens = debugTokens;
  }

  /**
   * Whether to disable automatic fixing of indentation. Only applicable for
   * linter mode FIX, otherwise ignored.
   *
   * @param disableIndentationFixing {@code true} to disable automatic
   *     indentation fixing. Defaults to {@code false}.
   */
  public void setDisableIndentationFixing(boolean disableIndentationFixing) {
    this.disableIndentationFixing = disableIndentationFixing;
  }

  /**
   * Whether to show error exceptions.
   *
   * @param errorTrace {@code true} to show error exceptions. Defaults to
   *     {@code false}.
   */
  public void setErrorTrace(boolean errorTrace) {
    this.errorTrace = errorTrace;
  }

  /**
   * Sets the fixjsstyle.py Python script file. Setting this attribute is not
   * necessary if {@code fixjsstyle} is defined on your PATH.
   *
   * @param fixjsstyle the fixjsstyle Python script
   */
  public void setFixjsstylePythonScript(String fixjsstyle) {
    this.fixjsstylePythonScript = fixjsstyle;
  }

  /**
   * Sets whether Closure Linter should always execute. If {@code true},
   * Closure Linter will always run. If {@code false}, then if the last
   * execution of Closure Linter did not yield any errors or warnings and the
   * last modified date for all of the specified source files precedes the
   * date and time that Closure Linter last ran, then execution will be
   * skipped.
   *
   * @param force {@code True} to force execution of Closure Linter. Defaults
   *     to {@code false}.
   */
  public void setForce(boolean force) {
    this.force = force;
  }

  /**
   * Sets the gjslint.py Python script file. Setting this attribute is not
   * necessary if {@code gjslint} is defined on your PATH.
   *
   * @param gjslint the gjslint Python script
   */
  public void setGjslintPythonScript(String gjslint) {
    this.gjslintPythonScript = gjslint;
  }

  /**
   * Sets the linter mode to either FIX or LINT. In FIX mode, simple JavaScript
   * style guide violations are automatically fixed. In LINT mode, style guide
   * violations are reported but no source files are changed.
   *
   * <p><b>WARNING:</b> Back up your files or store them in a source control
   * system before using FIX mode in case the script makes unwanted changes.
   * </p>
   *
   * @param linterMode the Closure Linter mode. Options: FIX or LINT
   */
  public void setLinterMode(String linterMode) {
    if ("FIX".equalsIgnoreCase(linterMode)) {
      this.linterMode = ClosureLinterMode.FIX;
    } else if ("LINT".equalsIgnoreCase(linterMode)) {
      this.linterMode = ClosureLinterMode.LINT;
    } else {
      throw new BuildException("linterMode expected to be FIX or LINT but "
          + "was " + linterMode);
    }
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
   * Whether to parallalize linting using the multiprocessing module. Disabled
   * by default.
   *
   * @param multiProcess {@code true} to use multiprocessing. Defaults to
   *     {@code false}.
   */
  public void setMultiProcess(boolean multiProcess) {
    this.multiProcess = multiProcess;
  }

  /**
   * Sets the Python interpreter executable. If Closure Linter is installed
   * such that {@code gjslint} and {@code fixjsstyle} are defined on your PATH,
   * then the Python executable should not be set. Only set the executable if
   * explicitly specifying the Python scripts {@code gjslint.py} and {@code
   * fixjsstyle.py}.
   *
   * @param python the Python executable, for example, {@code python}
   */
  public void setPythonExecutable(String python) {
    this.pythonExecutable = python;
  }

  /**
   * Whether to print the full command line used to run gjslint or
   * fixjsstyle.
   *
   * @param showCommandLine {@code true} to print the command line. Defaults
   *     to {@code false}.
   */
  public void setShowCommandLine(boolean showCommandLine) {
    this.showCommandLine = showCommandLine;
  }

  /**
   * Whether to show an error count summary.
   *
   * @param showSummary {@code true} to show an error count summary. Defaults
   *     to {@code false}.
   */
  private void setShowSummary(boolean showSummary) {
    this.showSummary = showSummary;
  }

  /**
   * Whether to emit timing statistics.
   *
   * @param timingStats {@code true} to emit timing stats. Defaults to {@code
   *     false}.
   */
  private void setTimingStats(boolean timingStats) {
    this.timingStats = timingStats;
  }

  /**
   * Whether to emit warnings in standard unix format.
   *
   * @param unixMode {@code true} to emit warnings in standard unix format.
   *     Defaults to {@code false}.
   */
  private void setUnixMode(boolean unixMode) {
    this.unixMode = unixMode;
  }


  // Nested element setters

  /**
   * Adds a list of additional JavaScript file extensions (other than "js")
   * separated by whitespace and/or commas. These additional file extensions
   * are only used if root directories are specified with the {@literal
   * <roots>} nested element.
   *
   * @param fileExtensionList a list of additional JavaScript file extensions
   */
  public void addConfiguredJSFileExtensionList(
      FileExtensionList fileExtensionList) {
    this.additionalJSFileExtensions.addAll(
        fileExtensionList.getFileExtensions());
  }

  /**
   * Adds error flag controlling how the linter handles various errors.
   *
   * @param linterErrors flags controlling the handling of errors
   * @throws BuildException if {@literal <closurelintererrors>} nested element
   *     already used in the current task
   */
  public void addClosureLinterErrors(ClosureLinterErrors linterErrors) {
    if (this.closureLinterErrors == null) {
      this.closureLinterErrors = linterErrors;
    } else {
      throw new BuildException("nested element <closurelintererrors> may "
          + "only be used once per <" + getTaskName() + "> task");
    }
  }

  /**
   * Adds a list of custom JavaScript doc tags separated by whitespace and/or
   * commas.
   *
   * @param docTagList a list of custom JavaScript doc tags
   */
  public void addConfiguredCustomJsDocTagList(DocTagList docTagList) {
    this.customJsDocTags.addAll(docTagList.getDocTags());
  }

  /**
   * Adds a list of fully qualified namespaces that should not be reported as
   * extra by the linter regardless of whether they are actually used. The
   * namespaces may be separated by whitespace and/or commas.
   *
   * @param ignoredExtraNamespaces a list of custom JavaScript doc tags
   */
  public void addConfiguredIgnoredExtraNamespacesList(
      NamespaceList ignoredExtraNamespaces) {
    this.extraNamespacesToIgnore.addAll(ignoredExtraNamespaces.getNamespaces());
  }

  /**
   * Adds "main" sources (that is, program entry points) for which transitive
   * dependencies will be calculated.
   *
   * @param mainSources program entry points
   */
  public void addMainSources(FileSet mainSources) {
    this.mainSources.add(mainSources);
  }

  /**
   * A list of namespaces separated by whitespace and/or commas that represent
   * program entry points for which transitive dependencies will be calculated.
   *
   * @param namespaces a list of Closure namespaces
   */
  public void addConfiguredNamespaceList(NamespaceList namespaces) {
    this.namespaces.addAll(namespaces.getNamespaces());
  }

  /**
   * Adds root directories to be recursively scanned for JavaScript source
   * files. By default, only the directory specified with the {@code dir}
   * attribute is scanned. If includes and/or excludes patterns are specified,
   * directories are recursively scanned for matching subdirectories. See
   * {@link RestrictedDirSet}.
   *
   * @param roots directories to be recursively scanned for JavaScript sources
   */
  public void addRoots(RestrictedDirSet roots) {
    this.roots.add(roots);
  }

  /**
   * Adds source files that are not considered program entry points. See {@link
   * #addMainSources(org.apache.tools.ant.types.FileSet)}.
   *
   * @param sourceFiles source files to lint
   */
  public void addSources(FileSet sourceFiles) {
    this.sources.add(sourceFiles);
  }

  /**
   * Adds source files with relaxed documentation checks. For example,
   * the following errors will not be reported for these files.
   *
   * <p><ul>
   * <li>Missing documentation</li>
   * <li>Missing descriptions</li>
   * <li>Methods whose {@code @return} tags do not have a matching {@code
   * return} statement</li>
   * </ul></p>
   *
   * @param sourceFiles source files with relaxed documentation checks
   */
  public void addSourcesWithRelaxedDocChecks(FileSet sourceFiles) {
    this.sourcesWithRelaxedDocumentationChecks.add(sourceFiles);
  }


  /**
   * Executes the Closure Linter task.
   *
   * @throws org.apache.tools.ant.BuildException on error.
   */
  public void execute() {

    CommandLineBuilder cmdline = null;
    Set<String> allSourcePaths = null;
    try {
      cmdline = createCommandLineFromTaskSettings();
      allSourcePaths = getAllSourcePaths();
    } catch (IOException e) {
      throw new BuildException(e);
    }

    boolean skipBuild = false;
    BuildCache cache = new BuildCache(this);

    if (!this.force) {
      // The Closure Linter build may be skipped if the following three
      // conditions are satisfied: 1) the last Closure Linter build produced
      // zero errors, 2) the specified settings for the command line did not
      // change, and 3) none of the source files for the current build has
      // been modified since the last build.

      BuildSettings previousBuild = cache.get();

      if (previousBuild != null && !previousBuild.isBuildFailed()) {
        if (previousBuild.getCommandLineOrConfig().equals(cmdline.toString())) {
          if (sourcesUpToDate(allSourcePaths, previousBuild.getBuildTime())) {
            skipBuild = true;
          }
        }
      }
    }

    if (!skipBuild) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      DefaultLogger logger = changeDefaultLoggerOutputStream(outputStream);

      LogStreamHandler logStreamHandler;
      logStreamHandler = new LogStreamHandler(this, Project.MSG_INFO,
          Project.MSG_WARN);
      Execute runner = new Execute(logStreamHandler);
      runner.setVMLauncher(false);
      runner.setAntRun(getProject());
      runner.setCommandline(cmdline.toStringArray());
      if (this.showCommandLine) {
        log("Executing command: " + cmdline.toString());
      }
      executeClosureLinter(runner);

      String antOutput = outputStream.toString();

      // Remove task identifier from log output. For example: [closure-linter]
      Pattern taskLabel = Pattern.compile("(?s)\\[" + getTaskName() + "\\] ");
      Matcher taskMatcher = taskLabel.matcher(antOutput);
      StringBuffer buffer = new StringBuffer();
      while (taskMatcher.find()) {
        taskMatcher.appendReplacement(buffer, "");
      }
      taskMatcher.appendTail(buffer);
      antOutput = buffer.toString();

      // If errors were found, suggest using linterMode FIX instead of the
      // default message to use fixjsstyle directly.
      if (runner.isFailure()
          && ClosureLinterMode.LINT.equals(this.linterMode)) {
        Pattern p = Pattern.compile("(?ms)Some of the errors reported.*"
            + "fixjsstyle .*$");
        Matcher m = p.matcher(antOutput);
        StringBuffer sb = new StringBuffer();
        if (m.find()) {
          m.appendReplacement(sb, "Some of the errors reported by Closure "
              + "Linter may be auto-fixable by using linterMode FIX. Back up "
              + "your files or store them in a source control system before "
              + "using FIX mode in case the script makes unwanted changes.");
        }
        m.appendTail(sb);
        antOutput = sb.toString();
      }

      // Restore Ant's default output stream to standard out.
      logger.setOutputPrintStream(new PrintStream(new FileOutputStream(
          FileDescriptor.out)));
      log(antOutput);

      if (this.logFile != null) {
        try {
          Files.write(antOutput, this.logFile, Charsets.UTF_8);
        } catch (IOException e) {
          throw new BuildException(e);
        }
      }

      if (!this.force) {
        // Save current build settings.
        BuildSettings currentBuildSettings = new BuildSettings(
            cmdline.toString(), allSourcePaths);
        currentBuildSettings.setBuildFailed(runner.isFailure());
        cache.put(currentBuildSettings);
      }
      if (runner.isFailure()) {
        String executableScript = ClosureLinterMode.LINT.equals(this.linterMode)
            ? this.gjslintPythonScript : this.fixjsstylePythonScript;
        throw new BuildException(executableScript + " finished with exit code "
            + runner.getExitValue());
      }
    }
  }

  /**
   * Changes the output stream used by the current Ant project's {@link
   * org.apache.tools.ant.DefaultLogger}.
   *
   * @param outputStream the output stream to use for the default logger
   * @return the default logger
   * @throws BuildException if the default logger's output stream cannot be
   *     changed
   */
  private DefaultLogger changeDefaultLoggerOutputStream(OutputStream outputStream) {
    DefaultLogger logger = null;

    for (Object obj : getProject().getBuildListeners()) {
      if (obj instanceof DefaultLogger) {
        PrintStream printStream = new PrintStream(outputStream);
        logger = (DefaultLogger) obj;
        logger.setOutputPrintStream(printStream);
      } else if (("class org.apache.ant.antunit.listener."
          + "BaseAntUnitListener$LogGrabber").equals(
          obj.getClass().toString())) {
        // The Ant task is being executed by AntUnit for testing.
        Project project = getProject();
        project.removeBuildListener((BuildListener)obj);
        logger = new DefaultLogger();
        logger.setMessageOutputLevel(Project.MSG_INFO);
        logger.setOutputPrintStream(new PrintStream(outputStream));
        project.addBuildListener(logger);
      }
    }

    if (logger == null) {
      throw new BuildException("unable to change the default Ant logger "
          + "output stream");
    }
    return logger;
  }

  /**
   * Create the command line to execute either the gjslint or fixjsstyle Python
   * script with the appropriate flags based on the Ant task settings.
   *
   * @return the command line
   * @throws IOException if one of the main sources cannot be read
   */
  private CommandLineBuilder createCommandLineFromTaskSettings()
      throws IOException {
    CommandLineBuilder cmdline = new CommandLineBuilder();

    if (this.pythonExecutable != null) {
      cmdline.argument(this.pythonExecutable);
    }

    if (ClosureLinterMode.LINT.equals(this.linterMode)) {
      cmdline.argument(this.gjslintPythonScript);
    } else {
      cmdline.argument(this.fixjsstylePythonScript);
    }

    // Attributes

    if(Boolean.TRUE.equals(this.beep)) {
      cmdline.argument("--beep");
    } else if (Boolean.FALSE.equals(this.beep)) {
      cmdline.argument("--nobeep");
    }
    if(Boolean.TRUE.equals(this.checkJavaScriptInHtmlFiles)) {
      cmdline.argument("--check_html");
    } else if (Boolean.FALSE.equals(this.checkJavaScriptInHtmlFiles)) {
      cmdline.argument("--nocheck_html");
    }
    if(Boolean.TRUE.equals(this.debugIndentation)) {
      cmdline.argument("--debug_indentation");
    } else if (Boolean.FALSE.equals(this.debugIndentation)) {
      cmdline.argument("--nodebug_indentation");
    }
    if(Boolean.TRUE.equals(this.debugTokens)) {
      cmdline.argument("--debug_tokens");
    } else if (Boolean.FALSE.equals(this.debugTokens)) {
      cmdline.argument("--nodebug_tokens");
    }
    if(Boolean.TRUE.equals(this.disableIndentationFixing)) {
      cmdline.argument("--disable_indentation_fixing");
    } else if (Boolean.FALSE.equals(this.disableIndentationFixing)) {
      cmdline.argument("--nodisable_indentation_fixing");
    }
    if(Boolean.TRUE.equals(this.errorTrace)) {
      cmdline.argument("--error_trace");
    } else if (Boolean.FALSE.equals(this.errorTrace)) {
      cmdline.argument("--noerror_trace");
    }
    if(Boolean.TRUE.equals(this.multiProcess)) {
      cmdline.argument("--multiprocess");
    } else if (Boolean.FALSE.equals(this.multiProcess)) {
      cmdline.argument("--nomultiprocess");
    }
    if(Boolean.TRUE.equals(this.showSummary)) {
      cmdline.argument("--summary");
    } else if (Boolean.FALSE.equals(this.showSummary)) {
      cmdline.argument("--nosummary");
    }
    if(Boolean.TRUE.equals(this.timingStats)) {
      cmdline.argument("--time");
    } else if (Boolean.FALSE.equals(this.timingStats)) {
      cmdline.argument("--notime");
    }
    if(Boolean.TRUE.equals(this.unixMode)) {
      cmdline.argument("--unix_mode");
    } else if (Boolean.FALSE.equals(this.unixMode)) {
      cmdline.argument("--nounix_mode");
    }

    // Nested elements

    cmdline.flagAndArguments("--additional_extensions",
        this.additionalJSFileExtensions);
    cmdline.commandLineBuilder(
        this.closureLinterErrors.getCommandLineForErrorFlags());
    cmdline.flagAndArguments("--custom_jsdoc_tags", this.customJsDocTags);
    cmdline.flagAndArguments("--ignored_extra_namespaces",
        this.extraNamespacesToIgnore);
    cmdline.flagAndArguments("--closurized_namespaces", this.namespaces);

    // Get the goog.provided namespaces from the main sources and pass to
    // Closure Linter with flag --closurized_namespaces.
    List<String> mainSourcePaths = AntUtil.getFilePathsFromCollectionOfFileSet(
        getProject(), this.mainSources);

    for (String mainSourcePath : mainSourcePaths) {
      JsClosureSourceFile jsFile = SourceFileFactory
          .newJsClosureSourceFile(new File(mainSourcePath));
      cmdline.flagAndArguments("--closurized_namespaces", jsFile.getProvides());
    }

    // Process <roots> nested elements.
    List<File> rootDirectories = Lists.newArrayList();
    for (RestrictedDirSet dirSet : this.roots) {
      rootDirectories.addAll(dirSet.getMatchedDirectories());
    }
    cmdline.flagAndArguments("--recurse", rootDirectories);

    List<String> sourcePathsRelaxedDocChecks = AntUtil
        .getFilePathsFromCollectionOfFileSet(
            getProject(), this.sourcesWithRelaxedDocumentationChecks);
    cmdline.flagAndArguments("--limited_doc_files",
        sourcePathsRelaxedDocChecks);

    List<String> sourcePaths = AntUtil.getFilePathsFromCollectionOfFileSet(
        getProject(), this.sources);

    Set<String> allSourcesExcludingRootDirs = Sets.newHashSet();

    allSourcesExcludingRootDirs.addAll(mainSourcePaths);
    allSourcesExcludingRootDirs.addAll(sourcePaths);
    allSourcesExcludingRootDirs.addAll(sourcePathsRelaxedDocChecks);
    cmdline.arguments(allSourcesExcludingRootDirs);

    return cmdline;
  }

  /**
   * Creates a set of all source files that will be passed to Closure Linter,
   * including files located anywhere below the specified root directories
   * with file extension ".js" or any of the extensions specified by nested
   * {@code jsFileExtensionList} elements.
   *
   * @return a set of all source paths to be passed to Closure Linter
   * @throws IOException if error scanning root directories
   */
  private Set<String> getAllSourcePaths() throws IOException {
    Set<String> allSources = Sets.newHashSet();

    List<String> mainSourcePaths = AntUtil.getFilePathsFromCollectionOfFileSet(
        getProject(), this.mainSources);
    allSources.addAll(mainSourcePaths);

    List<String> sourcePaths = AntUtil.getFilePathsFromCollectionOfFileSet(
        getProject(), this.sources);
    allSources.addAll(sourcePaths);

    List<String> sourcePathsRelaxedDocChecks = AntUtil
        .getFilePathsFromCollectionOfFileSet(
            getProject(), this.sourcesWithRelaxedDocumentationChecks);
    allSources.addAll(sourcePathsRelaxedDocChecks);

    // Process <roots> nested elements.
    List<File> rootDirectories = Lists.newArrayList();
    for (RestrictedDirSet dirSet : this.roots) {
      rootDirectories.addAll(dirSet.getMatchedDirectories());
    }

    // TODO(cpeisert): normalize extensions to either retain or strip dot prefix
    Set<String> jsFileExtensions = Sets.newHashSet();
    jsFileExtensions.addAll(this.additionalJSFileExtensions);
    jsFileExtensions.add(".js");

    for (File dir : rootDirectories) {
      for (String extension : jsFileExtensions) {
        List<String> paths = FileUtil.scanDirectory(dir,
            /* includes */ ImmutableList.of("**/*" + extension),
            /* excludes */ ImmutableList.of(".*"));
        allSources.addAll(paths);
      }
    }

    return allSources;
  }

  /**
   * Check if a collection of source files are up-to-date relative to
   * some specified time.
   *
   * @param sources source files to check
   * @param timeInMilliseconds the reference time for comparing the source
   *     files' last modified time
   * @return {@code true} if the source files are up-to-date relative to the
   *     specified time
   */
  private boolean sourcesUpToDate(Collection<String> sources,
                                  Long timeInMilliseconds) {
    for (String filePath : sources) {
      if (new File(filePath).lastModified() > timeInMilliseconds) {
        return false;
      }
    }

    return true;
  }

  /**
   * Executes gjslint or fixjsstyle depending on the Closure Linter mode.
   *
   * @param runner the {@link org.apache.tools.ant.taskdefs.Execute} runner
   * @return the exit code returned by either gjslint or fixjsstyle
   * @throws org.apache.tools.ant.BuildException if there is an
   *     {@link IOException}
   */
  private int executeClosureLinter(Execute runner) {
    int exitCode = 0;
    try {
      exitCode = runner.execute();
    } catch (IOException e) {
      throw new BuildException(e);
    }
    return exitCode;
  }
}
