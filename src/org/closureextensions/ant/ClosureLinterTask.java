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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

  private final static String FIXJSSTYLE = "fixjsstyle";
  private final static String GJSLINT = "gjslint";
  private final static String PYTHON = "python";

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
    this.pythonExecutable = PYTHON;
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

  // TODO(cpeisert): add setters for missing field



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
      // TODO(cpeisert): use outputStream to capture Ant log to logFile
      /*ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      if(!changeDefaultLoggerOutputStream(outputStream)) {
        throw new BuildException("unable to change the default logger's output "
            + "stream");
      }*/

      LogStreamHandler logStreamHandler;
      logStreamHandler = new LogStreamHandler(this, Project.MSG_INFO,
          Project.MSG_WARN);
      Execute runner = new Execute(logStreamHandler);
      runner.setVMLauncher(false);
      runner.setAntRun(getProject());
      runner.setCommandline(cmdline.toStringArray());
      int exitCode = executeClosureLinter(runner);

      if (!this.force) {
        // Save current build settings.
        BuildSettings currentBuildSettings = new BuildSettings(
            cmdline.toString(), allSourcePaths);
        boolean buildFailed = exitCode != 0;
        currentBuildSettings.setBuildFailed(buildFailed);
        cache.put(currentBuildSettings);
      }
    }
  }

  /**
   * TODO(cpeisert): Delete this method if not needed
   * Changes the output stream used by the current Ant project's {@link
   * org.apache.tools.ant.DefaultLogger}.
   *
   * @param outputStream the output stream to use for the default logger
   * @return {@code true} if the default logger's output stream was
   *     successfully changed
   */
  /*private boolean changeDefaultLoggerOutputStream(OutputStream outputStream) {
    for (Object obj : getProject().getBuildListeners()) {
      if (obj instanceof DefaultLogger) {
        PrintStream printStream = new PrintStream(outputStream);
        ((DefaultLogger) obj).setOutputPrintStream(printStream);
        return true;
      }
    }
    return false;
  }*/

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

    // TODO(cpeisert): test on windows to determine if "python" must be
    // explicitly specified on command line
    //cmdline.argument(this.pythonExecutable);

    if (ClosureLinterMode.LINT.equals(this.linterMode)) {
      cmdline.argument(this.gjslintPythonScript);
    } else {
      cmdline.argument(this.fixjsstylePythonScript);
    }

    // Attributes

    if(Boolean.TRUE.equals(this.beep)) {
      cmdline.argument("--beep");
    }
    if(Boolean.TRUE.equals(this.checkJavaScriptInHtmlFiles)) {
      cmdline.argument("--check_html");
    }
    if(Boolean.TRUE.equals(this.debugIndentation)) {
      cmdline.argument("--debug_indentation");
    }
    if(Boolean.TRUE.equals(this.debugTokens)) {
      cmdline.argument("--debug_tokens");
    }
    if(Boolean.TRUE.equals(this.disableIndentationFixing)) {
      cmdline.argument("--disable_indentation_fixing");
    }
    if(Boolean.TRUE.equals(this.errorTrace)) {
      cmdline.argument("--error_trace");
    }
    if(Boolean.TRUE.equals(this.multiProcess)) {
      cmdline.argument("--multiprocess");
    }
    if(Boolean.TRUE.equals(this.showSummary)) {
      cmdline.argument("--summary");
    }
    if(Boolean.TRUE.equals(this.timingStats)) {
      cmdline.argument("--time");
    }
    if(Boolean.TRUE.equals(this.unixMode)) {
      cmdline.argument("--unix_mode");
    }

    // Nested elements

    Set<String> allSourcesExcludingRootDirs = Sets.newHashSet();

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

    allSourcesExcludingRootDirs.addAll(mainSourcePaths);
    allSourcesExcludingRootDirs.addAll(sourcePaths);
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
      if (exitCode != 0) {
        String executableScript = ClosureLinterMode.LINT.equals(this.linterMode)
            ? this.gjslintPythonScript : this.fixjsstylePythonScript;
        log("Error: " + this.pythonExecutable + " " + executableScript
            + " finished with exit code " + exitCode);
      }
    } catch (IOException e) {
      throw new BuildException(e);
    }
    return exitCode;
  }
}
