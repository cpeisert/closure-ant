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
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import org.closureextensions.common.util.AntUtil;
import org.closureextensions.common.util.ClosureBuildUtil;
import org.closureextensions.common.util.FileUtil;

/**
 * Closure Stylesheets Ant task. The default task name is {@code stylesheets}
 * as defined in "task-definitions.xml".
 *
 * <p>The location of the Closure Stylesheets jar file is also defined in
 * "closure-tools-config.xml", which should be included in your build file as
 * follows:</p>
 *
 * <p>{@literal <import file="your/path/to/closure-tools-config.xml" />}</p>
 *
 * <p><i>Verify that the paths defined in "closure-tools-config.xml" are
 * correct for your local configuration.</i></p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ClosureStylesheetsTask extends Task {

  // Attributes
  private String copyrightNotice;
  private boolean forceRecompile;
  private Path gssFunctionMapProviderClassName;
  private Path gssFunctionMapProviderClasspath;
  private String inputManifest;
  private String inputOrientation;
  private String outputFile;
  private String outputOrientation;
  private String outputRenamingMap;
  private String outputRenamingMapFormat;
  private Boolean prettyPrint;
  private String renamingType;
  private File stylesheetsJar;

  // Nested elements
  private final List<FileSet> cssFileSets;
  private final List<String> excludedClassesFromRenaming;
  private final List<String> trueConditions;

  /**
   * Constructs a new Closure Compiler Ant task.
   */
  public ClosureStylesheetsTask() {
    this(null);
  }

  /**
   * Constructs a new bound Closure Compiler Ant task. This is useful when
   * wrapping the {@link org.closureextensions.ant.ClosureStylesheetsTask} within another task as follows:
   *
   * <p><pre>{@code
   * ClosureCompilerTask compilerTask = ClosureCompilerTask(this);
   * }</pre></p>
   */
  public ClosureStylesheetsTask(Task owner) {
    super();
    if (owner != null) {
      bindToOwner(owner);
    }

    // Attributes
    this.copyrightNotice = null;
    this.forceRecompile = false;
    this.gssFunctionMapProviderClassName = null;
    this.gssFunctionMapProviderClasspath = null;
    this.inputManifest = null;
    this.inputOrientation = null;
    this.outputFile = null;
    this.outputOrientation = null;
    this.outputRenamingMap = null;
    this.outputRenamingMapFormat = null;
    this.prettyPrint = null;
    this.renamingType = null;
    this.stylesheetsJar = null;

    // Nested elements
    this.cssFileSets = Lists.newArrayList();
    this.excludedClassesFromRenaming = Lists.newArrayList();
    this.trueConditions = Lists.newArrayList();
  }


  // Attribute setters

  /** @param file the Closure Stylesheets jar file */
  public void setStylesheetsJar(File file) {
    this.stylesheetsJar = file;
  }
  
  /**
   * Forces recompilation even if the output CSS file is up-to-date.
   *
   * @param forceRecompile determines if the CSS sources should always be
   *     recompiled even if none of the source files or compiler options have
   *     changed since the {@code outputFile} was last modified. Defaults to
   *     {@code false}.
   */
  public void setForceRecompile(boolean forceRecompile) {
    this.forceRecompile = forceRecompile;
  }

  /**
   * Specifies a file containing a list of CSS sources to be included
   * in the compilation, where each line in the manifest is a file path.
   *
   * @param inputManifest the input manifest file name
   */
  public void setInputManifest(String inputManifest) {
    this.inputManifest = inputManifest;
  }

  /**
   * The output CSS filename. If empty, standard output will be used. The
   * output is always UTF-8 encoded.
   *
   * @param outputFile the file to write output to instead of standard output
   */
  public void setOutputFile(String outputFile) {
    this.outputFile = outputFile;
  }


  // Nested element setters

  /**
   * An Ant <a href="http://ant.apache.org/manual/Types/fileset.html">FileSet
   * </a> containing CSS/GSS files to be compiled.
   *
   * @param cssFileSet a {@link FileSet} containing CSS/GSS files to be
   *     compiled
   */
  public void addCssFileSet(FileSet cssFileSet) {
    this.cssFileSets.add(cssFileSet);
  }  


  /**
   * Execute the Closure Stylesheets task.
   *
   * @throws BuildException on error.
   */
  public void execute() {

    // Verify task preconditions

    if (this.stylesheetsJar == null) {
      String stylesheetsJarPath =
          SharedAntProperty.CLOSURE_STYLESHEETS_JAR.getValue(getProject());
      if (stylesheetsJarPath != null) {
        this.stylesheetsJar = new File(stylesheetsJarPath);
      } else {
        throw new BuildException("\"stylesheetsJar\" is not set. Verify "
            + "that your build file imports \"closure-tools-config.xml\" and "
            + "that the property locations are correct for your machine.");
      }
    }

    // Execute Closure Compiler.

    Java runner = new Java(this);
    runner.setJar(this.stylesheetsJar);
    runner.setFailonerror(true);
    runner.setFork(true);
    runner.setLogError(true);
    runner.setTaskName(getTaskName());

    CommandLineBuilder cmdlineFlags = getCommandLineOptionsExcludingSources();
    List<String> cssCurrentSources = getAllSources();

    for (String arg : cmdlineFlags.toListOfString()) {
      runner.createArg().setValue(arg);
    }
    for (String cssSource : cssCurrentSources) {
      runner.createArg().setValue(cssSource);
    }

    boolean skipCompilation = false;

    if (!this.forceRecompile) {
      // Check if the output file is up-to-date.

      BuildCache cache = new BuildCache(this);
      String currentCommandLineAndCompilerFlags =
          runner.getCommandLine().toString() + " " + cmdlineFlags.toString();
      BuildSettings previousBuildSettings = cache.get();
      BuildSettings currentBuildSettings = new BuildSettings(
          currentCommandLineAndCompilerFlags, cssCurrentSources);
      // Save current build settings for the comparison with the next build.
      cache.put(currentBuildSettings);

      if (previousBuildSettings != null) {
        if (ClosureBuildUtil.outputFileUpToDate(new File(this.outputFile),
            previousBuildSettings, currentBuildSettings)) {
          skipCompilation = true;
          log("Output file up-to-date. Stylesheet compilation skipped.");
        }
      }
    }

    if (!skipCompilation) {
      String sheetOrSheets = (cssCurrentSources.size() > 1) ? "stylesheets"
          : "stylesheet";
      log("Compiling " + cssCurrentSources.size() + " " + sheetOrSheets
          + "...");


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
    CommandLineBuilder cmdline = new CommandLineBuilder();

    /*if (this.manageClosureDependencies != null) {
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
    */
    return cmdline;
  }

  /**
   * Creates a list of CSS sources based on the {@code inputManifest} file if
   * specified as well as nested {@literal <cssfileset>} elements.
   *
   * @return a list of the current CSS build sources
   * @throws BuildException if there is an {@link java.io.IOException} reading
   *     {@code inputManifest}
   */
  private List<String> getAllSources() {
    List<String> currentBuildSources = Lists.newArrayList();

    if (this.inputManifest != null) {
      currentBuildSources.addAll(FileUtil.readlines(
          new File(this.inputManifest)));
    }
    currentBuildSources.addAll(
        AntUtil.getFilePathsFromCollectionOfFileSet(getProject(),
            this.cssFileSets));

    return currentBuildSources;
  }
}
