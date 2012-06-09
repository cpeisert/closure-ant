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
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import org.closureextensions.ant.types.CompilationLevel;
import org.closureextensions.ant.types.CompilerOptionsComplete;
import org.closureextensions.ant.types.CompilerOptionsFactory;
import org.closureextensions.ant.types.Directory;
import org.closureextensions.ant.types.NamespaceList;
import org.closureextensions.ant.types.StringNestedElement;
import org.closureextensions.builderplus.BuilderPlusUtil;
import org.closureextensions.builderplus.OutputMode;
import org.closureextensions.common.CssRenamingMap;
import org.closureextensions.common.deps.ManifestBuilder;
import org.closureextensions.common.JsClosureSourceFile;
import org.closureextensions.common.SourceFileFactory;
import org.closureextensions.common.util.AntUtil;
import org.closureextensions.common.util.FileUtil;

/**
 * Builder Plus Ant task. Builder Plus is similar to Closure Builder,
 * except that the "list" mode has been replaced with "manifest" mode and the
 * "script" mode has been renamed "raw" to match plovr. In addition, Builder
 * Plus is implemented in Java, whereas Closure Builder is implemented in
 * Python. The default task name is {@code builder-plus} as defined in
 * "task-definitions.xml".
 *
 * <p>The location of the Closure Compiler is defined in
 * "closure-tools-config.xml", which should be included in your build file as
 * follows:</p>
 *
 * <p>{@literal <import file="your/path/to/closure-tools-config.xml" />}</p>
 *
 * <p><i>Verify that the paths defined in "closure-tools-config.xml" are correct
 * for your local configuration.</i></p>
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
 *     {@link org.closureextensions.ant.types.ClosureCompiler}</td></tr>
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
public final class BuilderPlusTask extends Task {

  // Attributes
  private File compilerJar;
  private CssRenamingMap cssRenamingMap;
  private boolean forceRecompile;
  private File inputManifest;
  private boolean keepAllSources;
  private boolean keepMoochers;
  private boolean keepOriginalOrder;
  private File outputFile;
  private File outputManifest;
  private OutputMode outputMode;

  // Nested elements
  private CompilerOptionsComplete compilerOptions;
  private final List<FileSet> mainSources; // Program entry points
  private final List<String> namespaces;
  private final List<Directory> roots;
  private final List<FileSet> sources;


  /**
   * Constructs a new Ant task for Closure Builder.
   */
  public BuilderPlusTask() {
    // Attributes
    this.compilerJar = null;
    this.cssRenamingMap =null;
    this.forceRecompile = false;
    this.inputManifest = null;
    this.keepAllSources = false;
    this.keepMoochers = false;
    this.keepOriginalOrder = false;
    this.outputFile = null;
    this.outputManifest = null;
    this.outputMode = OutputMode.COMPILED;

    // Nested elements
    this.compilerOptions = null;
    this.mainSources = Lists.newArrayList();
    this.namespaces = Lists.newArrayList();
    this.roots = Lists.newArrayList();
    this.sources = Lists.newArrayList();
  }


  // Attribute setters

  /**
   * Sets the Closure Compiler jar file.
   *
   * @param file the Closure Compiler jar file
   */
  public void setCompilerJar(File file) {
    this.compilerJar = file;
  }

  /**
   * Creates a new {@link CssRenamingMap} populated from the JSON in
   * the specified file. The JSON object must have keys and values of type
   * string (i.e. no numbers, boolean values, nested arrays, or objects).
   *
   * <p>Any characters outside the outermost matching set of curly braces
   * are ignored. This flexibility means that all of the CSS renaming map
   * output formats supported by <a target="_blank"
   * href="http://code.google.com/p/closure-stylesheets/">Closure Stylesheets
   * </a> (with the exception of Java {@link java.util.Properties}) may be
   * passed directly to this attribute (i.e. JSON, CLOSURE_COMPILED, and
   * CLOSURE_UNCOMPILED). To set the CSS renaming map from a Java {@link
   * Properties} file, see {@link
   * #setCssRenamingMapPropertiesFile(java.io.File)}.</p>
   *
   * <p>The format makes no difference, since in all cases the JSON is parsed
   * into a {@link CssRenamingMap}. Builder Plus handles the CSS renaming
   * map as follows:</p>
   *
   * <p><ul>
   * <li><b>Builder Plus output mode: COMPILED</b></li>
   *   <li><ul>
   *     <li><b>Compilation level: SIMPLE or ADVANCED</b> - creates a temp file
   *     using the Closure Stylesheets output renaming map format
   *     CLOSURE_COMPILED. This file immediately follows base.js in the
   *     manifest.</li>
   *     <li><b>Compilation level: WHITESPACE_ONLY</b> - creates a temp file
   *     using the Closure Stylesheets output renaming map format
   *     CLOSURE_UNCOMPILED. This file immediately precedes base.js in the
   *     manifest.</li>
   *   </ul></li>
   * <li><b>Builder Plus output mode: MANIFEST</b> - works the same as output
   *     mode COMPILED. If a nested {@literal <compile>} element is not
   *     specified, the compilation level defaults to SIMPLE.</li>
   * <li><b>Builder Plus output mode: RAW</b> - creates a renaming map using
   *     the Closure Stylesheets output renaming map format CLOSURE_UNCOMPILED
   *     and prepends it to the concatenated output.</li>
   * </ul></p>
   *
   * @param renamingMap CSS renaming map file path
   * @throws ClassCastException if JSON object contains non-string values such
   *     as number or boolean
   * @throws BuildException if the CSS renaming map has already been set
   * @throws java.io.IOException if there is an error reading the file
   * @throws com.google.gson.JsonParseException if the file does not contain
   *     valid JSON
   */
  public void setCssRenamingMap(String renamingMap) throws IOException {
    if (this.cssRenamingMap == null) {
      this.cssRenamingMap =
          CssRenamingMap.createFromJsonFile(new File(renamingMap));
    } else {
      throw new BuildException("cssRenamingMap already set");
    }
  }

  /**
   * Creates a new {@link CssRenamingMap} populated from the specified Java
   * {@link Properties} file. See {@link #setCssRenamingMap(String)} for
   * details on how the CSS renaming map is added to the manifest.
   *
   * @param propertiesFile Java {@link Properties} file from which to create
   *     CSS renaming map
   * @throws BuildException if the CSS renaming map has already been set
   * @throws java.io.IOException if there is an error reading the file
   */
  public void setCssRenamingMapPropertiesFile(File propertiesFile)
      throws IOException {
    if (this.cssRenamingMap == null) {
      this.cssRenamingMap =
          CssRenamingMap.createFromJavaPropertiesFile(propertiesFile);
    } else {
      throw new BuildException("cssRenamingMap already set");
    }
  }

  /**
   * Determines if the Closure Compiler should always recompile the {@code
   * outputFile}, even if none of the input files (externs or sources) have
   * changed since the {@code outputFile} was last modified.
   *
   * @param forceRecompile if {@code true}, always recompile the {@code
   *     outputFile}, even if none of the input files (externs or sources)
   *     have changed. Defaults to {@code false}.
   */
  public void setForceRecompile(boolean forceRecompile) {
    this.forceRecompile = forceRecompile;
  }

  /**
   * Specifies a file containing a list of JavaScript sources to be included
   * in the compilation, where each line in the manifest is a file path.
   *
   * @param inputManifest the input manifest file
   */
  public void setInputManifest(File inputManifest) {
    this.inputManifest = inputManifest;
  }

  /**
   * Whether all sources should be passed to the Closure Compiler, i.e.,
   * no sources are pruned irrespective of the transitive dependencies of
   * the program entry points. This option is useful for compiling libraries.
   *
   * @param keepAllSources whether all sources should be passed to the
   *     Closure Compiler. Defaults to {@code false}.
   */
  public void setKeepAllSources(boolean keepAllSources) {
    this.keepAllSources = keepAllSources;
  }

  /**
   * Whether "moochers" (i.e. source files that do not provide any namespaces,
   * though they may goog.require namespaces) may be dropped during the
   * dependency pruning process. If {@code true}, these files are always kept
   * as well as any files they depend on. If {@code false}, these files may be
   * dropped during dependency pruning.
   *
   * <p><b>Note: </b> this option has no effect if {@link #keepAllSources} is
   * set to {@code true} (i.e. if dependency pruning is turned off).</p>
   *
   * <p>See <a target="_blank"
   * href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/DependencyOptions.java">
   * DependencyOptions.java</a></p>
   *
   * @param keepMoochers if {@code true}, moochers and their dependencies are
   *     always kept. Defaults to {@code false}.
   */
  public void setKeepMoochers(boolean keepMoochers) {
    this.keepMoochers = keepMoochers;
  }

  /**
   * Whether sources should be kept in their original order or topologically
   * sorted based on their dependencies.
   *
   * @param keepOriginalOrder if {@code true}, sources will be kept in their
   *     original order, otherwise they will be topologically sorted based on
   *     their dependencies. Defaults to {@code false}.
   */
  public void setKeepOriginalOrder(boolean keepOriginalOrder) {
    this.keepOriginalOrder = keepOriginalOrder;
  }

  /**
   * Sets an output file to use instead of standard output.
   *
   * @param file the file to write output to instead of standard output
   */
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
   * Sets the output mode. Output is sent to standard out by default. In
   * COMPILED and RAW mode, output may be sent to a file by setting {@link
   * #outputFile}. In MANIFEST mode, output may be sent to a file by settings
   * {@link #outputManifest}.
   *
   * @param mode the output mode. Options: COMPILED, MANIFEST, RAW. Defaults
   *     to COMPILED.
   * @throws BuildException if {@code mode} is not a valid option
   */
  public void setOutputMode(String mode) {
    if (OutputMode.COMPILED.toString().equalsIgnoreCase(mode)) {
      this.outputMode = OutputMode.COMPILED;
    } else if (OutputMode.MANIFEST.toString().equalsIgnoreCase(mode)) {
      this.outputMode = OutputMode.MANIFEST;
    } else if (OutputMode.RAW.toString().equalsIgnoreCase(mode)) {
      this.outputMode = OutputMode.RAW;
    } else {
      throw new BuildException("Attribute \"outputMode\" expected to be "
          + "one of COMPILED, MANIFEST, or RAW but was \"" + mode + "\"");
    }
  }


  // Nested element setters

  /**
   * @return a new instance of {@link CompilerOptionsComplete}
   * @throws BuildException if {@literal <compiler>} nested element already
   *     used in the current Closure Builder Ant task
   */
  public CompilerOptionsComplete createCompiler() {
    if (this.compilerOptions == null) {
      this.compilerOptions =
          CompilerOptionsFactory.newCompilerOptionsComplete();
    } else {
      throw new BuildException("nested element <compiler> may only be used "
          + "once per <" + getTaskName() + "> task");
    }
    return this.compilerOptions;
  }

  /** @param mainSources program entry points */
  public void addMainSources(FileSet mainSources) {
    this.mainSources.add(mainSources);
  }

  /**
   * A list of namespaces separated by whitespace and/or commas that represent
   * program entry points for which dependencies will be calculated.
   *
   * @param namespaces a list of Closure namespaces
   */
  public void addConfiguredNamespaceList(NamespaceList namespaces) {
    this.namespaces.addAll(namespaces.getNamespaces());
  }

  /** @param root path to be traversed to build the dependencies */
  public void addRoot(Directory root) {
    this.roots.add(root);
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
   * Execute the Builder Plus task.
   *
   * @throws BuildException on error.
   */
  public void execute() {
    try {// execute() cannot throw checked IOException due to parent definition
      File manifestFile;
      List<String> manifestList = createManifest();
      String manifestString = Joiner.on(String.format("%n")).skipNulls()
          .join(manifestList);

      if (this.outputManifest != null) {
        Files.write(manifestString, this.outputManifest, Charsets.UTF_8);
        manifestFile = this.outputManifest;
      } else {
        // Save a copy of the manifest in directory '.closure-ant-tasks'.
        BuildCache cache = new BuildCache(this);
        manifestFile = cache.createTempFile("temp_manifest_for_target["
            + getOwningTarget().getName() + "].txt");
        Files.write(manifestString, manifestFile, Charsets.UTF_8);
      }

      if (OutputMode.COMPILED == this.outputMode) {
        runClosureCompiler(manifestFile);
      }
      if (OutputMode.MANIFEST == this.outputMode) {
        if (this.outputManifest == null) {
          log(manifestString);
        }
      }
      if (OutputMode.RAW == this.outputMode) {
        writeRawConcatenationOfSources(manifestList);
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
   * @throws IOException if a source file cannot be read
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
   * Run the Closure Compiler with the help of the {@link ClosureCompilerTask}.
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
            + "Compiler is required for output mode COMPILED. Verify "
            + "that your build file imports \"closure-tools-config.xml\" and "
            + "that the property locations are correct for your machine.");
      }
    }

    ClosureCompilerTask compilerTask = new ClosureCompilerTask(this);
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
      throw new BuildException("manifest file was null");
    }
    compilerTask.execute();
  }

  /**
   * Creates a manifest suitable for the Closure Compiler. Such a manifest is
   * an ordered list of JavaScript source files derived from the transitive
   * dependencies of the program entry points. Program entry points are
   * specified as either namespaces or "main" sources (i.e. source files
   * that must be included in the manifest). The transitive dependencies are
   * defined by calls to {@code goog.provide()} and {@code goog.require()}. A
   * stable topological sort is used to make sure that an input always comes
   * after its dependencies, unless the flag {@link #keepOriginalOrder} is set
   * to {@code true}, in which case the sources are not sorted.
   *
   * <p>If a CSS renaming map is specified, it will be written to a temporary
   * file and added to the manifest. See {@link #setCssRenamingMap(String)}.</p>
   *
   * @return a manifest list of sources after dependency management
   * @throws BuildException if {@link ManifestBuilder} throws a dependency
   *     related exception such as {@link
   *     org.closureextensions.common.deps.CircularDependencyException}
   * @throws IOException if there errors reading source files or writing the
   *     manifest file
   */
  private List<String> createManifest() throws IOException {
    // Source-file entry points.
    List<JsClosureSourceFile> sourceEntryPoints = Lists.newArrayList();

    // Additional sources (may be pruned if not transitively required by the
    // entry points).
    List<JsClosureSourceFile> sources = Lists.newArrayList();

    log("Scanning paths...");

    List<String> paths = null;

    // Process inputManifest attribute.
    if (this.inputManifest != null) {
      paths = Files.readLines(this.inputManifest, Charsets.UTF_8);
      for (String path : paths) {
        JsClosureSourceFile sourceFile =
            SourceFileFactory.newJsClosureSourceFile(new File(path));
        sourceEntryPoints.add(sourceFile);
      }
    }

    // Process <mainSources> nested elements.
    paths = AntUtil.getFilePathsFromCollectionOfFileSet(
        getProject(), this.mainSources);
    for (String path : paths) {
      JsClosureSourceFile sourceFile =
          SourceFileFactory.newJsClosureSourceFile(new File(path));
      sourceEntryPoints.add(sourceFile);
    }

    // Process <sources> nested elements.
    paths =
        AntUtil.getFilePathsFromCollectionOfFileSet(getProject(), this.sources);
    for (String path : paths) {
      JsClosureSourceFile sourceFile =
          SourceFileFactory.newJsClosureSourceFile(new File(path));
      sources.add(sourceFile);
    }

    // Process <root> nested elements.
    for (Directory dir : this.roots) {
      paths = FileUtil.scanDirectory(dir.getDirectory(),
          /* includes */ ImmutableList.of("**/*.js"),
          /* excludes */ ImmutableList.of(".*"));
      for (String path : paths) {
        JsClosureSourceFile sourceFile =
            SourceFileFactory.newJsClosureSourceFile(new File(path));
        sources.add(sourceFile);
      }
    }

    ManifestBuilder<JsClosureSourceFile> builder =
        new ManifestBuilder<JsClosureSourceFile>();
    builder.mainSources(sourceEntryPoints);
    builder.sources(sources);
    builder.namespaces(this.namespaces)
        .keepAllSources(this.keepAllSources)
        .keepMoochers(this.keepMoochers)
        .keepOriginalOrder(this.keepOriginalOrder);

    log(builder.getAllSourcesInOriginalOrder().size() + " sources scanned.");

    log("Building dependency tree...");

    List<JsClosureSourceFile> manifestList = null;

    try {
      manifestList = builder.toManifestList();
    } catch (Exception e) {
      throw new BuildException(e);
    }

    if (this.cssRenamingMap != null && !this.cssRenamingMap.isEmpty()) {
      CompilationLevel level = (this.compilerOptions == null) ?
          CompilationLevel.SIMPLE_OPTIMIZATIONS :
          this.compilerOptions.getCompilationLevel();
      File outputDir = new BuildCache(this).getBaseDirectory();
      JsClosureSourceFile tempRenamingMap =
          BuilderPlusUtil.createRenamingMapFileAndAddToManifest(
              this.cssRenamingMap, this.outputMode, level, manifestList,
              outputDir);
      log("Adding temporary CSS renaming map to manifest... ["
          + tempRenamingMap.getAbsolutePath() + "]");
    }

    List<String> manifestFilePaths = Lists.newArrayList();

    for (JsClosureSourceFile jsClosureSourceFile : manifestList) {
      if (!jsClosureSourceFile.getAbsolutePath().isEmpty()) {
        manifestFilePaths.add(jsClosureSourceFile.getAbsolutePath());
      } else {
        manifestFilePaths.add(jsClosureSourceFile.getName());
      }
    }

    log(manifestFilePaths.size() + " dependencies in final manifest.");

    return manifestFilePaths;
  }
}
