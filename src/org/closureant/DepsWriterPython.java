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

package org.closureant;

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

import org.closureant.base.SharedAntProperty;
import org.closureant.base.CommandLineBuilder;
import org.closureant.deps.DirectoryPathPrefixPair;
import org.closureant.deps.FilePathDepsPathPair;
import org.closureant.util.AntUtil;
import org.closureant.util.StringUtil;

/**
 * DepsWriter Ant task. This task is a wrapper for the original Python script
 * "depswriter.py"(located in closure-library/closure/bin/build). The default
 * task name is {@code deps-writer-python} as defined in "task-definitions.xml".
 *
 * <p>The locations of the Closure Library and DepsWriter Python script
 * are defined in "closure-ant-config.xml", which should be included in your
 * build file as follows:</p>
 *
 * <p>{@literal <import file="your/path/to/closure-ant-config.xml" />}</p>
 *
 * <p><i>Verify that the paths defined in "closure-ant-config.xml" are
 * correct for your local configuration.</i></p>
 *
 * <p>For more information about DepsWriter, see
 * <a target="_blank"
 * href="https://developers.google.com/closure/library/docs/depswriter">
 * Using DepsWriter</a>.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class DepsWriterPython extends Task {

  // Attributes
  private File depsWriterPythonScript;
  private File outputFile;
  private String pythonExecutable;

  // Nested elements
  private final List<FilePathDepsPathPair> paths;
  private final List<DirectoryPathPrefixPair> roots;


  /**
   * Constructs a new Ant task for Deps Writer.
   */
  public DepsWriterPython() {
    // Attributes
    this.depsWriterPythonScript = null;
    this.outputFile = null;
    this.pythonExecutable = "python";

    // Nested elements    
    this.paths = Lists.newArrayList();
    this.roots = Lists.newArrayList();
  }


  // Attribute setters

  /** @param file the Deps Writer Python script */
  public void setDepsWriterPythonScript(File file) {
    this.depsWriterPythonScript = file;
  }

  /** @param file the file to write output to instead of standard output */
  public void setOutputFile(File file) {
    this.outputFile = file;
  }

  /** @param python the Python interpreter executable. Defaults to "python".*/
  public void setPythonExecutable(String python) {
    this.pythonExecutable = python;
  }


  // Nested element setters

  /**
   * @param path a file path and an optional alternate path to the file
   *     in the generated deps file
   */
  public void addPath(FilePathDepsPathPair path) {
    this.paths.add(path);
  }

  /**
   * @param root a root path to scan for JavaScript source files and an
   *     optional prefix to use in the deps file
   */
  public void addRoot(DirectoryPathPrefixPair root) {
    this.roots.add(root);
  }

  /**
   * @param sourceFiles source files to add to the deps file
   */
  public void addConfiguredSources(FileSet sourceFiles) {
    List<File> files = AntUtil.getListOfFilesFromAntFileSet(getProject(),
        sourceFiles);
    for (File file : files) {
      this.paths.add(new FilePathDepsPathPair(file.getAbsolutePath(), null));
    }
  }

  /**
   * Executes the Deps Writer task.
   *
   * @throws BuildException on error.
   */
  @Override
  public void execute() {

    // Verify task preconditions

    if (this.depsWriterPythonScript == null) {
      String depsWriterScriptPath =
          SharedAntProperty.DEPS_WRITER_PY.getValue(getProject());
      if (depsWriterScriptPath != null) {
        this.depsWriterPythonScript = new File(depsWriterScriptPath);
      } else {
        throw new BuildException("\"depsWriterPythonScript\" is "
            + "not set. Verify that your build file imports "
            + "\"closure-ant-config.xml\" and that the property paths are "
            + "correct for your machine.");
      }
    }

    // Build the command line.

    CommandLineBuilder cmdline = new CommandLineBuilder();
    cmdline.argument(this.pythonExecutable);
    cmdline.argument(this.depsWriterPythonScript);
    if (this.outputFile != null) {
      cmdline.flagAndArgument("--output_file",
          this.outputFile.getAbsolutePath());
    }

    for (FilePathDepsPathPair pair : this.paths) {
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
    executeDepsWriter(runner);
  }

  /**
   * Execute depswriter.py.
   *
   * @param runner the {@link org.apache.tools.ant.taskdefs.Execute} runner
   * @return the exit code returned by depswriter.py
   * @throws BuildException if there is an {@link IOException}
   */
  private int executeDepsWriter(Execute runner) {
    int exitCode;
    try {
      exitCode = runner.execute();
      if (exitCode != 0) {
        log("Error: " + this.pythonExecutable + " "
            + this.depsWriterPythonScript + " finished with exit code "
            + exitCode);
      }
    } catch (IOException e) {
      throw new BuildException(e);
    }
    return exitCode;
  }
}