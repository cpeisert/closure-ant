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

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

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
import org.closureant.base.BuildCache;
import org.closureant.base.CommandLineBuilder;
import org.closureant.jscomp.CompleteCompilerOptions;
import org.closureant.jscomp.CompilerOptionsFactory;
import org.closureant.types.RestrictedDirSet;
import org.closureant.types.StringNestedElement;
import org.closureant.util.AntUtil;

/**
 * Ant task wrapping the Python script closurebuilder.py. The default task
 * name is {@code closure-builder} as defined in "task-definitions.xml".
 *
 * <p>The locations of the Closure Library and Closure Builder Python script
 * are defined in "closure-ant-config.xml", which should be included in your
 * build file as follows:</p>
 *
 * <p>{@literal <import file="your/path/to/closure-ant-config.xml" />}</p>
 *
 * <p><i>Verify that the paths defined in "closure-ant-config.xml" are
 * correct for your local configuration.</i></p>
 *
 * <p>For more information about Closure Builder, see
 * <a target="_blank"
 * href="https://developers.google.com/closure/library/docs/closurebuilder">
 * Using ClosureBuilder</a>.</p>
 *
 *
 * TODO(cpeisert): Move the Ant-style documentation below into separate doc
 * files.
 *
 * <ul class="blockList">
 * <li class="blockList">
 * <h3>Attributes</h3>
 *
 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0">
 * <col width="20%"/>
 * <col width="60%"/>
 * <col width="20%"/>
 * <thead>
 * <tr><th>Attribute Name</th><th>Description</th><th>Required</th></tr>
 * </thead>
 * <tbody>
 * <tr class="altColor"><td id="closureBuilderPythonScript">
 *     <b>closureBuilderPythonScript</b></td><td>The Closure Builder Python
 *     script.</td><td>No, as long as your build file imports
 *     closureextensions.xml</td></tr>
 * <tr class="rowColor"><td id="compilerJar"><b>compilerJar</b></td><td>The
 *     Closure Compiler jar file.</td><td>No, as long as your build file
 *     imports closureextensions.xml</td></tr>
 * <tr class="rowColor"><td id="forceRecompile"><b>forceRecompile</b></td><td>
 *     Determines if the Closure Compiler should always recompile the output
 *     file, even if none of the input files have changed since the output
 *     file was last modified.</td><td>No. Defaults to {@code false}.</td></tr>
 * <tr class="altColor"><td id="inputManifest"><b>inputManifest</b></td><td>
 *     Specifies a file containing a list of file paths to JavaScript sources
 *     to be included in the compilation, where each line in the manifest is
 *     a file path.</td><td>No</td></tr>
 * <tr class="altColor"><td id="outputFile"><b>outputFile</b></td><td>Output
 *     file name. If not specified, write to standard output.</td><td>No</td>
 *     </tr>
 * <tr class="altColor"><td id="outputManifest"><b>outputManifest</b></td><td>
 *     Prints out a list of all the files in the compilation. This will not
 *     include files that got dropped because they were not required.</td>
 *     <td>No</td></tr>
 * <tr class="rowColor"><td id="outputMode"><b>outputMode</b></td><td>The
 *     type of output to generate. Options are "script" for a single script
 *     containing the contents of all the files concatenated together or
 *     "compiled" to produce compiled output with the Closure Compiler.
 *     Unlike the closurebuilder.py command line interface, there is no "list"
 *     output mode. Instead, a manifest may be saved by setting the attribute
 *     "outputManifest".</td>
 *     <td>No. Defaults to "compile".</td></tr>
 * <tr class="altColor"><td id="pythonExecutable"><b>pythonExecutable</b></td>
 *     <td>The python interpreter executable.</td><td>No. Defaults to
 *     "python".</td></tr>
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
 * <col width="20%"/>
 * <col width="80%"/>
 * <thead>
 * <tr><th>Element Name</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr class="altColor"><td id="compiler"><b>compiler</b></td><td>Options for
 *     the Closure Compiler. For documentation see
 *     {@link org.closureant.jscomp.ClosureCompiler}</td></tr>
 * <tr class="rowColor"><td id="inputs"><b>inputs</b></td><td>Input files to
 *    be compiled. Each input file and its transitive dependencies will be
 *    included in the compiled output. The {@literal <inputs>} element is an
 *    Ant <a href="http://ant.apache.org/manual/Types/fileset.html">FileSet</a>
 *    (i.e. it supports FileSet's attributes and nested elements).
 *
 *    <p><b>Note</b>: <i>As of February 2012, the closurebuilder.py Python
 *    script does not include files passed to the {@code --input} flag as
 *    source files for the build but merely extracts the provided namespaces.
 *    However, this Ant task includes {@literal <inputs>} in the sources to
 *    be compiled, similar to how plovr treats its
 *    <a href="http://plovr.com/options.html#inputs">inputs</a> configuration
 *    option.</i></p></td></tr>
 * <tr class="altColor"><td id="namespace"><b>namespace</b></td><td>A namespace
 *     to calculate dependencies for. The {@literal <namespace>} element has a
 *     {@code value} attribute that accepts a Closure namespace. A Closure
 *     namespace is a dot-delimited path expression declared with a call to
 *     {@code goog.provide()} (for example, "goog.array" or "foo.bar").
 *     Namespaces provided by {@literal <namespace>} elements will be combined
 *     with those provided by {@literal <inputs>}.</td></tr>
 * <tr class="rowColor"><td id="roots"><b>roots</b></td><td>Roots are directory
 *     paths to be traversed to build dependencies. The {@literal <root>}
 *     element has a {@code directory} attribute to specify a directory path.
 *     </td></tr>
 * <tr class="altColor"><td id="sources"><b>sources</b></td><td>Sources are
 *     JavaScript source files available to the build process that will be
 *     used if they are transitively required by one of the {@code namespaces}
 *     or {@code inputs}. The {@literal <sources>} element is an Ant
 *     <a href="http://ant.apache.org/manual/Types/fileset.html">FileSet</a>
 *     (i.e. it supports FileSet's attributes and nested elements).</td></tr>
 * </tbody>
 * </table>
 * </li>
 * </ul>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ClosureBuilderPython extends Task {

  /**
   * Output modes supported by Closure Builder. Unlike the closurebuilder.py
   * command line interface, there is no "list" output mode. Instead, a
   * manifest may be saved by setting the attribute "outputManifest". The
   * default output mode is COMPILED. In addition, the "script" mode has been
   * renamed "raw" to match plovr.
   */
  public static enum OutputMode {
    /** Produces compiled output with the Closure Compiler. */
    COMPILED,
    /**
     * Produces a single script containing the concatenated contents of all
     * the files.
     */
    RAW,
    ;
  }

  // Attributes
  private File closureBuilderPythonScript;
  private File compilerJar;
  private boolean forceRecompile;
  private File inputManifest;
  private File outputFile;
  private File outputManifest;
  private OutputMode outputMode;
  private String pythonExecutable;

  // Nested elements
  private CompleteCompilerOptions compilerOptions;
  private final List<FileSet> inputs;
  private final List<StringNestedElement> namespaces;
  private final List<RestrictedDirSet> roots;
  private final List<FileSet> sources;


  /**
   * Constructs a new Ant task for Closure Builder.
   */
  public ClosureBuilderPython() {
    // Attributes
    this.closureBuilderPythonScript = null;
    this.compilerJar = null;
    this.forceRecompile = false;
    this.inputManifest = null;
    this.outputFile = null;
    this.outputManifest = null;
    this.outputMode = OutputMode.COMPILED;
    this.pythonExecutable = "python";

    // Nested elements
    this.compilerOptions = null;
    this.inputs = Lists.newArrayList();
    this.namespaces = Lists.newArrayList();
    this.roots = Lists.newArrayList();
    this.sources = Lists.newArrayList();
  }


  // Attribute setters

  /** @param file the Closure Builder Python script */
  public void setClosureBuilderPythonScript(File file) {
    this.closureBuilderPythonScript = file;
  }

  /** @param file the Closure Compiler jar file */
  public void setCompilerJar(File file) {
    this.compilerJar = file;
  }

  /**
   * @param forceRecompile determines if the Closure Compiler should always
   *     recompile the {@code outputFile}, even if none of the input files
   *     (externs or sources) have changed since the {@code outputFile} was
   *     last modified. Defaults to {@code false}.
   */
  public void setForceRecompile(boolean forceRecompile) {
    this.forceRecompile = forceRecompile;
  }

  /**
   * Specifies a file containing a list of file paths to JavaScript sources to
   * be included in the compilation, where each line in the manifest is a file
   * path.
   *
   * @param inputManifest the input manifest file
   */
  public void setInputManifest(File inputManifest) {
    this.inputManifest = inputManifest;
  }

  /** @param file the file to write output to instead of standard output */
  public void setOutputFile(File file) {
    this.outputFile = file;
  }

  /**
   * Prints out a list of all the files in the compilation. This will not
   * include files that got dropped because they were not required.
   *
   * @param outputManifest the output manifest file name
   */
  public void setOutputManifest(File outputManifest) {
    this.outputManifest = outputManifest;
  }

  /**
   * Set the output mode. Unlike the closurebuilder.py command line interface,
   * there is no "list" output mode. Instead, a manifest may be saved by
   * setting the attribute "outputManifest". See
   * {@link #setOutputManifest(File)}.
   *
   * @param mode the output mode. Options: "raw" or "compiled". Defaults
   *     to "compiled".
   * @throws BuildException if {@code mode} is not a valid option
   */
  public void setOutputMode(String mode) {
    if (OutputMode.RAW.toString().equalsIgnoreCase(mode)) {
      this.outputMode = OutputMode.RAW;
    } else if (OutputMode.COMPILED.toString().equalsIgnoreCase(mode)) {
      this.outputMode = OutputMode.COMPILED;
    } else {
      throw new BuildException("Attribute \"outputMode\" expected to be "
          + "either \"compiled\" or \"raw\", but was \"" + mode + "\"");
    }
  }

  /** @param python the Python interpreter executable. Defaults to "python".*/
  public void setPythonExecutable(String python) {
    this.pythonExecutable = python;
  }


  // Nested element setters

  /**
   * @return a new instance of {@link org.closureant.jscomp.CompleteCompilerOptions}
   * @throws BuildException if {@literal <compiler>} nested element already
   *     used in the current Closure Builder Ant task
   */
  public CompleteCompilerOptions createCompiler() {
    if (this.compilerOptions == null) {
      this.compilerOptions =
          CompilerOptionsFactory.newCompilerOptionsComplete();
    } else {
      throw new BuildException("nested element <compiler> may only be used "
          + "once per <" + getTaskName() + "> task");
    }
    return this.compilerOptions;
  }
  
  /** @param inputFiles files to calculate dependencies for */
  public void addInputs(FileSet inputFiles) {
    this.inputs.add(inputFiles);
  }

  /** @param namespace a Closure namespace */
  public void addNamespace(StringNestedElement namespace) {
    this.namespaces.add(namespace);
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
   * @param sourceFiles source files available to the build process that will
   *     be used if they are transitively required by one of the
   *     {@code namespaces} or {@code inputs}
   */
  public void addSources(FileSet sourceFiles) {
    this.sources.add(sourceFiles);
  }

  /**
   * Execute the Closure Builder task.
   *
   * @throws BuildException on error.
   */
  @Override
  public void execute() {
    if (this.closureBuilderPythonScript == null) {
      String closureBuilderScriptPath =
          SharedAntProperty.CLOSURE_BUILDER_PY.getValue(getProject());
      if (closureBuilderScriptPath != null) {
        this.closureBuilderPythonScript = new File(closureBuilderScriptPath);
      } else {
        throw new BuildException("\"closureBuilderPythonScript\" is "
            + "not set. Verify that your build file imports "
            + "\"closure-ant-config.xml\" and that the property paths are "
            + "correct for your machine.");
      }
    }

    File manifest = createManifest();
    List<String> currentSources;

    try {
      currentSources = Files.readLines(manifest, Charsets.UTF_8);

      if (this.outputManifest != null) {
        Joiner joiner = Joiner.on(String.format("%n")).skipNulls();
        Files.write(joiner.join(currentSources), this.outputManifest,
            Charsets.UTF_8);
      }

      if (OutputMode.RAW == this.outputMode) {
        writeRawConcatenationOfSources(currentSources);
      } else if (OutputMode.COMPILED == this.outputMode) {
        runClosureCompiler(manifest);
      }
    } catch (IOException e) {
      throw new BuildException(e);
    }
  }

  /**
   * Create a script comprised of the concatenated contents of {@code sources}.
   * The script will be written to {@link #outputFile} if set, otherwise to
   * standard output.
   *
   * @param sources the sources to concatenate
   */
  private void writeRawConcatenationOfSources(List<String> sources)
      throws IOException {
    StringBuilder rawScript = new StringBuilder();

    for (String path : sources) {
      rawScript.append(Files.toString(new File(path), Charsets.UTF_8));
    }
    if (this.outputFile != null) {
      Files.write(rawScript.toString(), this.outputFile, Charsets.UTF_8);
    } else {
      System.out.println(rawScript.toString());
    }
  }

  /**
   * Run the Closure Compiler based on the source manifest created by running
   * Closure Builder in "list" mode.
   *
   * @param manifest a manifest file listing all of the sources for the build
   * @throws BuildException if the manifest file is {@code null}
   */
  private void runClosureCompiler(File manifest) {
    if (this.compilerJar == null) {
      String closureCompilerPath =
          SharedAntProperty.CLOSURE_COMPILER_JAR.getValue(getProject());
      if (closureCompilerPath != null) {
        this.compilerJar = new File(closureCompilerPath);
      } else {
        throw new BuildException("\"compilerJar\" is not set. The Closure "
            + "Compiler is required for output mode \"compiled\". Verify "
            + "that your build file imports \"closure-ant-config.xml\" and "
            + "that the property locations are correct for your machine.");
      }
    }

    ClosureCompiler compilerTask = new ClosureCompiler(this);
    if (this.compilerOptions != null) {
      compilerTask.protectedSetCompilerOptions(this.compilerOptions);
    }

    compilerTask.setForceRecompile(this.forceRecompile);
    if (this.outputFile != null) {
      compilerTask.setOutputFile(this.outputFile.getAbsolutePath());
    }
    if (this.compilerJar != null) {
      compilerTask.setCompilerJar(this.compilerJar);
    }
    if (manifest != null) {
      compilerTask.setInputManifest(manifest.getAbsolutePath());
    } else {
      throw new BuildException("manifest file from closurebuilder.py was null");
    }
    compilerTask.execute();
  }

  /**
   * Creates a list of sources for the build by running closurebuilder.py in
   * "list" mode.
   *
   * @return a manifest file containing a list of the managed sources
   */
  private File createManifest() {
    CommandLineBuilder cmdline = new CommandLineBuilder();
    cmdline.argument(this.pythonExecutable);
    cmdline.argument(this.closureBuilderPythonScript);
    BuildCache cache = new BuildCache(this);
    File tempManifest = cache.createTempFile("temp_manifest.txt");
    cmdline.flagAndArgument("--output_file", tempManifest.getAbsolutePath());
    cmdline.flagAndArgument("--output_mode", "list");

    for (FileSet fileSet : this.inputs) {
      cmdline.flagAndFileSet("--input", fileSet, getProject());
    }
    for (StringNestedElement namespace : this.namespaces) {
      cmdline.flagAndArgument("--namespace", namespace.getValue());
    }
    // Add all source files to the command line.
    if (this.inputManifest != null) {
      try {
        cmdline.arguments(Files.readLines(this.inputManifest, Charsets.UTF_8));
      } catch (IOException e) {
        throw new BuildException(e);
      }
    }
    cmdline.arguments(
        AntUtil.getFilePathsFromCollectionOfFileSet(getProject(), this.inputs));
    cmdline.arguments(
        AntUtil.getFilePathsFromCollectionOfFileSet(getProject(),
            this.sources));

    // Process <roots> nested elements.
    List<File> rootDirectories = Lists.newArrayList();
    for (RestrictedDirSet dirSet : this.roots) {
      rootDirectories.addAll(dirSet.getMatchedDirectories());
    }

    for (File dir : rootDirectories) {
      cmdline.flagAndArgument("--root", dir.getAbsolutePath());
    }

    // Execute closurebuilder.py in "list" mode to generate the manifest.
    LogStreamHandler logStreamHandler = new LogStreamHandler(this,
        Project.MSG_INFO, Project.MSG_WARN);
    Execute runner = new Execute(logStreamHandler);
    runner.setVMLauncher(false);
    runner.setAntRun(getProject());
    runner.setCommandline(cmdline.toStringArray());
    try {
      int exitCode;
      exitCode = runner.execute();
      if (exitCode != 0) {
        log("Error: failed to generate temp manifest \""
            + tempManifest.getAbsolutePath() + "\". "
            + this.closureBuilderPythonScript + " finished with exit code "
            + exitCode);
      }
    } catch (IOException e) {
      throw new BuildException(e);
    }

    return tempManifest;
  }
}