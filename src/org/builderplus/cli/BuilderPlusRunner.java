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

package org.builderplus.cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

import org.builderplus.OutputMode;

import org.closureextensions.ant.ClosureCompilerTask;
import org.closureextensions.common.deps.ManifestBuilder;
import org.closureextensions.common.JsClosureSourceFile;
import org.closureextensions.common.SourceFileFactory;
import org.closureextensions.common.util.FileUtil;

/**
 * Builder Plus program runner. Builder Plus is similar to Closure Builder,
 * except that the "list" mode has been replaced with the option
 * "outputManifest" and the "script" mode has been renamed "raw" to match
 * plovr. There are also flags to control the dependency management process:
 * {@code --keep_all_sources}, {@code --keep_moochers}, and {@code
 * --keep_original_order}.
 *
 * @author cpeisert@gmail.com (Christopher Peisert)
 */
public final class BuilderPlusRunner {

  private File compilerJar;
  private boolean forceRecompile;
  private File inputManifest;
  private boolean keepAllSources;
  private boolean keepMoochers;
  private boolean keepOriginalOrder;
  private File outputFile;
  private File outputManifest;
  private OutputMode outputMode;

  private File flagFile;
  private final List<JsClosureSourceFile> mainSources; // Program entry points
  private final List<String> namespaces;
  private final List<String> roots;
  private final List<JsClosureSourceFile> sources;


  /**
   * Constructs a new Ant task for Closure Builder.
   */
  public BuilderPlusRunner(CommandLineOptions options) {

    this.compilerJar = options.getCompilerJar();
    this.forceRecompile = options.isForceRecompile();
    this.inputManifest = options.getInputManifest();
    this.keepAllSources = options.isKeepAllSources();
    this.keepMoochers = options.isKeepMoochers();
    this.keepOriginalOrder = options.isKeepOriginalOrder();
    this.outputFile = options.getOutputFile();
    this.outputManifest = options.getOutputManifest();
    this.outputMode = options.getOutputMode();

    Function<File, JsClosureSourceFile> fileToJsSourceFile =
        new Function<File, JsClosureSourceFile>() {
          @Override
          public JsClosureSourceFile apply(File source) {
            return SourceFileFactory.newJsSourceFile(source);
          }
        };

    this.flagFile = options.getFlagFile();
    // Need to call Lists.newArrayList() to obtain a mutable copy.
    this.mainSources = Lists.newArrayList(
        Lists.transform(options.getMainSources(), fileToJsSourceFile));
    this.namespaces = options.getNamespaces();
    this.roots = options.getRoots();
    this.sources = Lists.newArrayList(
        Lists.transform(options.getSources(), fileToJsSourceFile));
    this.sources.addAll(Lists.transform(options.getArguments(),
        fileToJsSourceFile));
  }

  /**
   * Execute Builder Plus.
   *
   * @throws org.closureextensions.common.deps.CircularDependencyException if
   *     the goog.provided and goog.required namespaces form a cycle
   * @throws org.closureextensions.common.deps.MissingProvideException if a
   *     goog.required namespace is not goog.provided by any of the inputs
   * @throws org.closureextensions.common.deps.MultipleProvideException if a
   *     namespace is provided by more than one source file
   * @throws IllegalStateException if the Closure Compiler jar file is null or
   *     does not exist
   * @throws NullPointerException if the manifest file returned by {@link
   * #createManifest()} is {@code null}
   */
  public void execute() {
    File manifest = createManifest();
    List<String> currentSources = FileUtil.readlines(manifest);
    Joiner manifestJoiner = Joiner.on(String.format("%n")).skipNulls();

    if (this.outputManifest != null) {
      FileUtil.write(manifestJoiner.join(currentSources), this.outputManifest);
    }

    if (OutputMode.COMPILED == this.outputMode) {
      runClosureCompiler(manifest);
    }
    if (OutputMode.MANIFEST == this.outputMode) {
      if (this.outputManifest == null) {
        System.out.println(manifestJoiner.join(currentSources));
      }
    }
    if (OutputMode.RAW == this.outputMode) {
      writeRawConcatenationOfSources(currentSources);
    }
  }

  /**
   * Create a script comprised of the concatenated contents of {@code sources}.
   * The script will be written to {@link #outputFile} if set, otherwise to
   * standard output.
   *
   * @param sources the sources to concatenate
   */
  private void writeRawConcatenationOfSources(List<String> sources) {
    StringBuilder rawScript = new StringBuilder();

    for (String path : sources) {
      rawScript.append(FileUtil.toString(new File(path)));
    }
    if (this.outputFile != null) {
      FileUtil.write(rawScript.toString(), this.outputFile);
    } else {
      System.out.println(rawScript.toString());
    }
  }

  /**
   * Run the Closure Compiler using the Closure Compiler Ant task bundled
   * with the Ant Closure Tools.
   *
   * @param manifest a manifest file listing all of the sources for the build
   * @throws IllegalStateException if the Closure Compiler jar file is null or
   *     does not exist
   * @throws NullPointerException if the manifest file is {@code null}
   */
  private void runClosureCompiler(File manifest) {
    Preconditions.checkNotNull(manifest, "manifest is null");
    if (this.compilerJar == null) {
      throw new IllegalStateException("\"compilerJar\" is not set. The Closure "
          + "Compiler is required for output mode COMPILED. Verify "
          + "that your build file imports \"closure-ant-tasks.xml\" and "
          + "that the property locations are correct for your machine.");
    }

    Project project = new Project();
    DefaultLogger consoleLogger = new DefaultLogger();
    consoleLogger.setErrorPrintStream(System.err);
    consoleLogger.setOutputPrintStream(System.out);
    consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
    project.addBuildListener(consoleLogger);
    project.init();

    Target builderPlusTarget = new Target();
    builderPlusTarget.setProject(project);
    builderPlusTarget.setName("_internal-builder-plus-target");
    ClosureCompilerTask compilerTask = new ClosureCompilerTask();
    compilerTask.setTaskName("closure-compiler");
    compilerTask.setProject(project);
    compilerTask.setOwningTarget(builderPlusTarget);

    compilerTask.setInputManifest(manifest.getAbsolutePath());
    if (this.flagFile != null) {
      compilerTask.setFlagFile(this.flagFile);
    }
    compilerTask.setForceRecompile(this.forceRecompile);
    if (this.outputFile != null) {
      compilerTask.setOutputFile(this.outputFile.getAbsolutePath());
    }
    if (this.compilerJar.exists()) {
      compilerTask.setCompilerJar(this.compilerJar);
    } else {
      throw new IllegalStateException("The Closure Compiler jar file "
          + "\"" + this.compilerJar.getAbsolutePath() + "\" does not exist.");
    }
    compilerTask.setInputManifest(manifest.getAbsolutePath());

    builderPlusTarget.addTask(compilerTask);
    project.addTarget(builderPlusTarget);
    builderPlusTarget.execute();
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
   * @return a manifest file containing a list of the sources after
   *     dependency management
   * @throws org.closureextensions.common.deps.CircularDependencyException if
   *     the goog.provided and goog.required namespaces form a cycle
   * @throws org.closureextensions.common.deps.MissingProvideException if a
   *     goog.required namespace is not goog.provided by any of the inputs
   * @throws org.closureextensions.common.deps.MultipleProvideException if a
   *     namespace is provided by more than one source file
   */
  private File createManifest() {
    List<JsClosureSourceFile> sourceEntryPoints = Lists.newArrayList(this.mainSources);

    System.out.println("Scanning paths...");

    List<String> paths = null;

    // Process --input_manifest flag
    if (this.inputManifest != null) {
      paths = FileUtil.readlines(this.inputManifest);
      for (String path : paths) {
        JsClosureSourceFile sourceFile =
            SourceFileFactory.newJsSourceFile(new File(path));
        sourceEntryPoints.add(sourceFile);
      }
    }

    // Process --root flags
    for (String dirPath : this.roots) {
      paths = FileUtil.scanDirectory(new File(dirPath),
          /* includes */ ImmutableList.of("**/*.js"),
          /* excludes */ ImmutableList.of(".*"));
      for (String path : paths) {
        JsClosureSourceFile sourceFile =
            SourceFileFactory.newJsSourceFile(new File(path));
        this.sources.add(sourceFile);
      }
    }

    ManifestBuilder<JsClosureSourceFile> builder =
        new ManifestBuilder<JsClosureSourceFile>();
    builder.mainSources(sourceEntryPoints);
    builder.sources(this.sources);
    builder.namespaces(this.namespaces)
        .keepAllSources(this.keepAllSources)
        .keepMoochers(this.keepMoochers)
        .keepOriginalOrder(this.keepOriginalOrder);

    System.out.println(builder.getAllSourcesInOriginalOrder().size()
        + " sources scanned.");

    System.out.println("Building dependency tree...");

    new File(".builder-plus").mkdir();
    File tempManifest = new File(".builder-plus/temp_manifest.txt");

    List<JsClosureSourceFile> manifestList = null;

    manifestList = builder.toManifestList();

    System.out.println(manifestList.size() + " dependencies in final "
        + "manifest.");

    FileUtil.write(
        Joiner.on(String.format("%n")).skipNulls().join(manifestList),
        tempManifest);

    return tempManifest;
  }
}