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
import org.apache.tools.ant.types.Reference;

import org.closureextensions.ant.types.AllowedNonStandardFunctionsList;
import org.closureextensions.ant.types.ClassNameList;
import org.closureextensions.ant.types.DefinedTrueConditionalsList;
import org.closureextensions.common.util.AntUtil;
import org.closureextensions.common.util.ClosureBuildUtil;
import org.closureextensions.common.util.FileUtil;

/**
 * Closure Stylesheets Ant task. The default task name is {@code stylesheets}
 * as defined in "task-definitions.xml".
 *
 * <p>The location of the Closure Stylesheets jar file is defined in
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
  private Boolean allowUnrecognizedFunctions;
  private String copyrightNotice;
  private boolean forceRecompile;
  private String gssFunctionMapProviderClassName;
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
  private final List<String> allowedNonStandardFunctions;
  private final List<FileSet> cssFileSets;
  private final List<String> classesExcludedFromRenaming;
  private final List<String> definedTrueConditionals;

  /**
   * Constructs a new Closure Stylesheets Ant task.
   */
  public ClosureStylesheetsTask() {
    this(null);
  }

  /**
   * Constructs a new bound Closure Stylesheets Ant task. This is useful when
   * using the {@link ClosureStylesheetsTask} within another task.
   */
  public ClosureStylesheetsTask(Task owner) {
    super();
    if (owner != null) {
      bindToOwner(owner);
    }

    // Attributes
    this.allowUnrecognizedFunctions = null;
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
    this.allowedNonStandardFunctions = Lists.newArrayList();
    this.cssFileSets = Lists.newArrayList();
    this.classesExcludedFromRenaming = Lists.newArrayList();
    this.definedTrueConditionals = Lists.newArrayList();
  }


  // Attribute setters

  /**
   * Whether to allow functions other than {@code url()} and {@code rgb()}.
   *
   * @param allowUnrecognizedFunctions {@code true} to allow unrecognized
   *     functions. Defaults to {@code false}.
   */
  public void setAllowUnrecognizedFunctions(
      boolean allowUnrecognizedFunctions) {
    this.allowUnrecognizedFunctions = allowUnrecognizedFunctions;
  }

  /**
   * Sets the classpath to use when searching for a custom GSS function map
   * provider. See <a target="_blank" href=
   * "http://ant.apache.org/manual/using.html#path">Path-like Structures</a>.
   *
   * @param classpath a class path
   */
  public void setClasspath(Path classpath) {
    if (this.gssFunctionMapProviderClasspath == null) {
      this.gssFunctionMapProviderClasspath = classpath;
    } else {
      this.gssFunctionMapProviderClasspath.append(classpath);
    }
  }

  /**
   * Creates a {@link org.apache.tools.ant.types.Path} classpath for a custom
   * GSS function map provider.
   *
   * @return a class path to be configured
   */
  public Path createClasspath() {
    if (this.gssFunctionMapProviderClasspath == null) {
      this.gssFunctionMapProviderClasspath = new Path(getProject());
    }
    return this.gssFunctionMapProviderClasspath.createPath();
  }

  /**
   * Adds a reference to a classpath defined elsewhere to use when searching
   * for a custom GSS function map provider.
   *
   * @param ref a reference to a classpath defined elsewhere (for example,
   *     in a {@literal <classpath>} element)
   */
  public void setClasspathRef(Reference ref) {
    createClasspath().setRefid(ref);
  }

  /**
   * Copyright notice to prepend to the output.
   *
   * @param copyrightNotice the copyright notice
   */
  public void setCopyrightNotice(String copyrightNotice) {
    this.copyrightNotice = copyrightNotice;
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
   * The fully qualified class name of a custom GSS function map provider to
   * resolve.
   *
   * @param className fully qualified class name of custom GSS function map
   *     provider. Defaults to {@code
   *     com.google.common.css.compiler.gssfunctions.DefaultGssFunctionMapProvider}.
   */
  public void setFunctionMapProvider(String className) {
    this.gssFunctionMapProviderClassName = className;
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
   * This specifies the display orientation the input files were written for.
   * The options are: LTR, RTL. LTR is the default and means that the input
   * style sheets were designed for use with left to right display User Agents.
   * RTL sheets are designed for use with right to left UAs. Currently, all
   * input files must have the same orientation, as there is no way to
   * specify the orientation on a per-file or per-library basis.
   *
   * @param inputOrientation the input orientation. Options: LTR, RTL.
   */
  public void setInputOrientation(String inputOrientation) {
    this.inputOrientation = inputOrientation;
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

  /**
   * Specify this option to perform automatic right to left conversion of the
   * input. The options are: LTR, RTL, NOCHANGE. NOCHANGE means the input will
   * not be changed in any way with respect to direction issues. LTR outputs a
   * sheet suitable for left to right display and RTL outputs a sheet suitable
   * for right to left display. If the input orientation is different than the
   * requested output orientation, 'left' and 'right' values in direction
   * sensitive style rules are flipped. If the input already has the desired
   * orientation, this option effectively does nothing except for defining
   * GSS_LTR and GSS_RTL, respectively. The input is LTR by default and can
   * be changed with the input_orientation flag.
   *
   * @param outputOrientation the output orientation. Options: LTR, RTL,
   *     NOCHANGE.
   */
  public void setOutputOrientation(String outputOrientation) {
    this.outputOrientation = outputOrientation;
  }

  /**
   * File to write the CSS class renaming map, which maps the original CSS
   * class names to their new names. See {@link
   * #setOutputRenamingMapFormat(String)}.
   *
   * @param outputRenamingMap file to write the CSS renaming map
   */
  public void setOutputRenamingMap(String outputRenamingMap) {
    this.outputRenamingMap = outputRenamingMap;
  }

  /**
   * The CSS renaming map format.
   *
   * <p>Options:</p>
   *
   * <p><ul>
   *
   * <li><b>CLOSURE_COMPILED</b> - should be used when compiling JavaScript
   * with the Closure Compiler in either SIMPLE or ADVANCED mode. When
   * CLOSURE_COMPILED is specified, the output is a JSON map of renaming
   * information wrapped in a call to {@code goog.setCssNameMapping()}.
   * <p><pre>{@code
goog.setCssNameMapping({
  "foo": "a",
  "bar": "b"
});
   * }</pre></p>
   * </li>
   *
   * <li><b>CLOSURE_UNCOMPILED</b> - should be used with uncompiled Closure
   * Library code. When CLOSURE_UNCOMPILED is specified, the output is a JSON
   * map of renaming information assigned to the global variable
   * CLOSURE_CSS_NAME_MAPPING.
   * <p><pre>{@code
CLOSURE_CSS_NAME_MAPPING = {
  "foo": "a",
  "bar": "b"
};
   * }</pre></p>
   * This file should be loaded via a {@literal <script>} tag before
   * {@code base.js} is loaded for the Closure Library. This is because
   * {@code base.js} checks to see whether the global CLOSURE_CSS_NAME_MAPPING
   * variable is declared, and if so, uses its value as the renaming data for
   * {@code goog.getCssName()}. This ensures that the mapping data is set
   * before any calls to {@code goog.getCssName()} are made.<p>&nbsp;</p>
   * </li>
   *
   * <li><b>JSON</b> - should be used when building a tool that consumes the
   * renaming-map data. When JSON is specified, the output is a pure JSON map.
   * <p><pre>{@code
{
  "foo": "a",
  "bar": "b"
}
   * }</pre></p>
   * </li>
   *
   * <li><b>PROPERTIES</b> - should be used as an alternative to JSON if, for
   * some reason, your toolchain does not have a JSON parser available. When
   * PROPERTIES is specified, the output is a .properties file formatted as
   * {@code key=value} pairs without any comments.
   * <p><pre>{@code
foo=a
bar=b
   * }</pre></p>
   * </li>
   *
   * </ul></p>
   *
   * <p>See Closure Stylesheets Wiki: <a target="_blank" href=
   * "http://code.google.com/p/closure-stylesheets/wiki/MoreOnCssRenaming#--output-renaming-map-format_Option">
   * <b>--output-renaming-map-format Option</b></a></p>
   *
   * @param outputRenamingMapFormat the output renaming map format. Options:
   *     CLOSURE_COMPILED, CLOSURE_UNCOMPILED, JSON, PROPERTIES.
   */
  public void setOutputRenamingMapFormat(String outputRenamingMapFormat) {
    this.outputRenamingMapFormat = outputRenamingMapFormat;
  }

  /**
   * Whether to format the output with newlines and indentation so that it is
   * more readable.
   *
   * @param prettyPrint {@code true} to "pretty print" the output. Defaults to
   *     {@code false}.
   */
  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  /**
   * How CSS classes should be renamed.
   *
   * <p>Options:</p>
   *
   * <p><ul>
   * <li><b>NONE</b> - should be used when no renaming should be done. This is
   * the default option.</li>
   * <li><b>DEBUG</b> -  should be used when debugging CSS renaming. Each CSS
   * class is trivially renamed by taking each part of the class name (where
   * parts are delimited by hyphens) and appending a trailing underscore.</li>
   * <li><b>CLOSURE</b> - should be used when minifying class names for
   * production. Each CSS class name is split into parts (as delimited by
   * hyphens), and then parts are renamed using the shortest available name.
   * Parts are renamed consistently, so if {@code .foo-bar} is renamed to
   * {@code .a-b}, then {@code .bar-foo} will be renamed to {@code .b-a}.</li>
   * </ul></p>
   *
   * <p>See Closure Stylesheets Wiki: <a target="_blank" href=
   * "http://code.google.com/p/closure-stylesheets/wiki/MoreOnCssRenaming#--rename_Option">
   * <b>--rename Option</b></a></p>
   *
   * @param renamingType the renaming type. Options: NONE, DEBUG, CLOSURE.
   *     Defaults to NONE.
   */
  public void setRenamingType(String renamingType) {
    this.renamingType = renamingType;
  }

  /** @param file the Closure Stylesheets jar file */
  public void setStylesheetsJar(File file) {
    this.stylesheetsJar = file;
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
   * A list of CSS class names that should not be renamed by the stylesheets
   * compiler. This may be necessary if some of your HTML is generated by a
   * process that does not take CSS renaming into account.
   *
   * <p><b>Note:</b> References to CSS class names that are excluded from
   * renaming should never be wrapped in {@code goog.getCssName()}, or else
   * they run the risk of being partially renamed.</p>
   *
   * @param classNameList list of CSS class names to exclude from renaming
   */
  public void addConfiguredClassesExcludedFromRenaming(
      ClassNameList classNameList) {
    this.classesExcludedFromRenaming.addAll(classNameList.getClassNames());
  }

  /**
   * Specifies the name of a {@code true} condition. The condition name can be
   * used in {@code @if} boolean expressions. The conditions are ignored if
   * GSS extensions are not enabled.
   *
   * @param trueConditionalsList list of conditionals to set to {@code true}
   */
  public void addConfiguredDefineTrueConditionals(
      DefinedTrueConditionalsList trueConditionalsList) {
    this.definedTrueConditionals.addAll(
        trueConditionalsList.getTrueConditionals());
  }

  /**
   * A whitelist of non-standard functions, like {@code alpha()}.
   *
   * @param functionsList list of non-standard functions to whitelist
   */
  public void addConfiguredAllowedNonStandardFunctions(
      AllowedNonStandardFunctionsList functionsList) {
    this.allowedNonStandardFunctions.addAll(
        functionsList.getAllowedNonStandardFunctions());
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
