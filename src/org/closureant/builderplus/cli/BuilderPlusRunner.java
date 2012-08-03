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

package org.closureant.builderplus.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

import org.closureant.ClosureCompilerTask;
import org.closureant.base.JsClosureSourceFile;
import org.closureant.base.SourceFileFactory;
import org.closureant.builderplus.BuilderPlusUtil;
import org.closureant.builderplus.OutputMode;
import org.closureant.css.CssRenamingMap;
import org.closureant.deps.ManifestBuilder;
import org.closureant.types.CompilationLevel;
import org.closureant.util.FileUtil;

import org.kohsuke.args4j.CmdLineException;

/**
 * Builder Plus program runner. Builder Plus is similar to Closure Builder,
 * except that the "list" mode has been replaced with the option
 * "outputManifest" and the "script" mode has been renamed "raw" to match
 * plovr. There are also flags to control the dependency management process:
 * {@code --keep_all_sources}, {@code --keep_moochers}, and {@code
 * --keep_original_order} as well as options to specify a CSS renaming map.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class BuilderPlusRunner {

  private static final int CACHE_EXPIRATION_DAYS = 2;

  private CssRenamingMap cssRenamingMap;
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
  public BuilderPlusRunner(CommandLineOptions options) throws CmdLineException {

    this.cssRenamingMap = options.getCssRenamingMap();
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
            try {
              return SourceFileFactory.newJsClosureSourceFile(source);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
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
   * @throws org.closureant.deps.CircularDependencyException if
   *     the goog.provided and goog.required namespaces form a cycle
   * @throws IllegalStateException if the Closure Compiler jar file is null or
   *     does not exist
   * @throws IOException if the build manifest cannot be written or read
   * @throws org.closureant.deps.MissingProvideException if a
   *     goog.required namespace is not goog.provided by any of the inputs
   * @throws org.closureant.deps.MultipleProvideException if a
   *     namespace is provided by more than one source file
   * @throws NullPointerException if the manifest file returned by {@link
   *     #createManifest(org.closureant.types.CompilationLevel, File)} is {@code null}
   */
  public void execute() throws IOException {
    ClosureCompilerTask compilerTask = null;
    CompilationLevel compilationLevel;

    if (OutputMode.COMPILED == this.outputMode) {
      compilerTask = newClosureCompilerTask();
      compilationLevel = compilerTask.getCompilationLevel();
    } else {
      compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    }

    File builderPlusCache = new File(".builder-plus");
    builderPlusCache.mkdir();
    FileUtil.deleteFilesOlderThanNumberOfDays(builderPlusCache, "*",
        CACHE_EXPIRATION_DAYS);
    List<String> manifestList = createManifest(compilationLevel,
        builderPlusCache);
    String manifestString = Joiner.on(String.format("%n")).skipNulls()
        .join(manifestList);

    File manifestFile;
    if (this.outputManifest != null) {
      Files.write(manifestString, this.outputManifest, Charsets.UTF_8);
      manifestFile = this.outputManifest;
    } else {
      // Save a copy of the manifest in directory '.builder-plus'.
      manifestFile = new File(builderPlusCache, "manifest.txt");
      Files.write(manifestString, manifestFile, Charsets.UTF_8);
    }

    if (compilerTask != null) {
      compilerTask.setInputManifest(manifestFile.getAbsolutePath());
      compilerTask.execute();
    }
    if (OutputMode.MANIFEST == this.outputMode) {
      if (this.outputManifest == null) {
        System.out.println(manifestString);
      }
    } else if (OutputMode.RAW == this.outputMode) {
      writeRawConcatenationOfSources(manifestList);
    }
  }

  /**
   * Create a script comprised of the concatenated contents of {@code sources}.
   * The script will be written to {@link #outputFile} if set, otherwise to
   * standard output.
   *
   * @param sources the sources to concatenate
   * @throws IOException if there is an error reading a source file
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
   * Creates a new {@link ClosureCompilerTask} for internal execution.
   *
   * <p><b>Note:</b> the input manifest is not set by this method and must
   * be set on the returned {@link ClosureCompilerTask}.</p>
   *
   * @throws IllegalStateException if the Closure Compiler jar file is null or
   *     does not exist
   */
  private ClosureCompilerTask newClosureCompilerTask() {
    if (this.compilerJar == null) {
      throw new IllegalStateException("\"compilerJar\" is not set. The Closure "
          + "Compiler is required for output mode COMPILED. Verify "
          + "that your build file imports \"closure-tools-config.xml\" and "
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

    return compilerTask;
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
   * file and added to the manifest. See {@link
   * org.closureant.BuilderPlusTask#setCssRenamingMap(String)}.</p>
   *
   * @param compilationLevel the Closure Compiler compilation level
   * @param outputDirectory directory to write temporary files, such as CSS
   *     renaming maps
   * @return a manifest file containing a list of the sources after
   *     dependency management
   * @throws org.closureant.deps.CircularDependencyException if
   *     the goog.provided and goog.required namespaces form a cycle
   * @throws IOException if there is an error reading or writing the build
   *     manifest
   * @throws org.closureant.deps.MissingProvideException if a
   *     goog.required namespace is not goog.provided by any of the inputs
   * @throws org.closureant.deps.MultipleProvideException if a
   *     namespace is provided by more than one source file
   */
  private List<String> createManifest(CompilationLevel compilationLevel,
      File outputDirectory) throws IOException {
    List<JsClosureSourceFile> sourceEntryPoints =
        Lists.newArrayList(this.mainSources);

    System.out.println("Scanning paths...");

    List<String> paths = null;

    // Process --input_manifest flag
    if (this.inputManifest != null) {
      paths = Files.readLines(this.inputManifest, Charsets.UTF_8);
      for (String path : paths) {
        JsClosureSourceFile sourceFile =
            SourceFileFactory.newJsClosureSourceFile(new File(path));
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
            SourceFileFactory.newJsClosureSourceFile(new File(path));
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

    List<JsClosureSourceFile> manifestList = builder.toManifestList();

    if (this.cssRenamingMap != null && !this.cssRenamingMap.isEmpty()) {
      JsClosureSourceFile tempRenamingMap =
          BuilderPlusUtil.createRenamingMapFileAndAddToManifest(
              this.cssRenamingMap, this.outputMode, compilationLevel,
              manifestList, outputDirectory);
      System.out.println("Adding temporary CSS renaming map to manifest... ["
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

    System.out.println(manifestFilePaths.size() + " dependencies in final "
        + "manifest.");

    return manifestFilePaths;
  }
}
