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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.css.ExitCodeHandler;
import com.google.common.css.GssFunctionMapProvider;
import com.google.common.css.JobDescription;
import com.google.common.css.JobDescription.InputOrientation;
import com.google.common.css.JobDescription.OutputOrientation;
import com.google.common.css.JobDescriptionBuilder;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.BasicErrorManager;
import com.google.common.css.compiler.gssfunctions.DefaultGssFunctionMapProvider;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import org.closureextensions.ant.types.AllowedNonStandardFunctionsList;
import org.closureextensions.ant.types.ClassNameList;
import org.closureextensions.ant.types.DefinedTrueConditionalsList;
import org.closureextensions.common.util.ClosureBuildUtil;
import org.closureextensions.css.ClosureStylesheetsCompiler;
import org.closureextensions.css.OutputRenamingMapFormat;
import org.closureextensions.css.RenamingType;

import javax.annotation.Nullable;

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
  private boolean allowUnrecognizedFunctions;
  private String copyrightNotice;
  private boolean forceRecompile;
  private String gssFunctionMapProviderClassName;
  private Path gssFunctionMapProviderClasspath;
  private String inputManifest;
  private JobDescription.InputOrientation inputOrientation;
  private String outputFile;
  private JobDescription.OutputOrientation outputOrientation;
  private String outputRenamingMap;
  private OutputRenamingMapFormat outputRenamingMapFormat;
  private boolean prettyPrint;
  private RenamingType renamingType;

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
    this.allowUnrecognizedFunctions = false;
    this.copyrightNotice = null;
    this.forceRecompile = false;
    this.gssFunctionMapProviderClassName = null;
    this.gssFunctionMapProviderClasspath = null;
    this.inputManifest = null;
    this.inputOrientation = InputOrientation.LTR;
    this.outputFile = null;
    this.outputOrientation = OutputOrientation.LTR;
    this.outputRenamingMap = null;
    this.outputRenamingMapFormat = OutputRenamingMapFormat.JSON;
    this.prettyPrint = false;
    this.renamingType = RenamingType.NONE;

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
   * @throws BuildException if {@code inputOrientation} is not a valid option
   */
  public void setInputOrientation(String inputOrientation) {
    if ("LTR".equalsIgnoreCase(inputOrientation)
        || "RTL".equalsIgnoreCase(inputOrientation)) {
      this.inputOrientation = InputOrientation.valueOf(
          inputOrientation.toUpperCase());
    } else {
      throw new BuildException("Attribute \"inputOrientation\" expected to be "
          + "LTR or RTL but was \"" + inputOrientation + "\"");
    }
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
   * @throws BuildException if {@code outputOrientation} is not a valid option
   */
  public void setOutputOrientation(String outputOrientation) {
    if ("LTR".equalsIgnoreCase(outputOrientation)
        || "RTL".equalsIgnoreCase(outputOrientation)
        || "NOCHANGE".equalsIgnoreCase(outputOrientation)) {
      this.outputOrientation = OutputOrientation.valueOf(
          outputOrientation.toUpperCase());
    } else {
      throw new BuildException("Attribute \"outputOrientation\" expected to "
          + "be LTR, RTL, or NOCHANGE but was \"" + outputOrientation + "\"");
    }
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
   * <p><pre><code>
goog.setCssNameMapping({
  "foo": "a",
  "bar": "b"
});
   * </code></pre></p>
   * </li>
   *
   * <li><b>CLOSURE_UNCOMPILED</b> - should be used with uncompiled Closure
   * Library code. When CLOSURE_UNCOMPILED is specified, the output is a JSON
   * map of renaming information assigned to the global variable
   * CLOSURE_CSS_NAME_MAPPING.
   * <p><pre><code>
CLOSURE_CSS_NAME_MAPPING = {
  "foo": "a",
  "bar": "b"
};
   * </code></pre></p>
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
   * <p><pre><code>
{
  "foo": "a",
  "bar": "b"
}
   * </code></pre></p>
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
   * @throws BuildException if {@code outputRenamingMapFormat} is not a valid
   *     option
   */
  public void setOutputRenamingMapFormat(String outputRenamingMapFormat) {
    if ("CLOSURE_COMPILED".equalsIgnoreCase(outputRenamingMapFormat)
        || "CLOSURE_UNCOMPILED".equalsIgnoreCase(outputRenamingMapFormat)
        || "JSON".equalsIgnoreCase(outputRenamingMapFormat)
        || "PROPERTIES".equalsIgnoreCase(outputRenamingMapFormat)) {
      this.outputRenamingMapFormat = OutputRenamingMapFormat.valueOf(
          outputRenamingMapFormat.toUpperCase());
    } else {
      throw new BuildException("Attribute \"outputRenamingMapFormat\" "
          + "expected to be one of CLOSURE_COMPILED, CLOSURE_UNCOMPILED, "
          + "JSON, or PROPERTIES but was\""+ outputRenamingMapFormat + "\"");
    }
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
    if ("CLOSURE".equalsIgnoreCase(renamingType)
        || "DEBUG".equalsIgnoreCase(renamingType)
        || "NONE".equalsIgnoreCase(renamingType)) {
      this.renamingType = RenamingType.valueOf(renamingType.toUpperCase());
    } else {
      throw new BuildException("Attribute \"renamingType\" expected to be "
          + "one of CLOSURE, DEBUG, or NONE but was \"" + renamingType + "\"");
    }
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
   * A whitelist of non-standard functions, such as {@code alpha()}.
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

    List<String> cssCurrentSources = getAllSources();
    String taskSettings = getAntTaskSettingsExcludingSourcesAsString();
    File cssOutputFile = null;

    if (!Strings.isNullOrEmpty(this.outputFile)) {
      cssOutputFile = new File(this.outputFile);
    }

    boolean skipCompilation = false;

    if (!this.forceRecompile && cssOutputFile != null) {
      // Check if the output file and output renaming map are up-to-date.

      BuildCache cache = new BuildCache(this);
      BuildSettings previousBuildSettings = cache.get();
      BuildSettings currentBuildSettings = new BuildSettings(
          taskSettings, cssCurrentSources);
      // Save current build settings for comparison with the next build.
      cache.put(currentBuildSettings);

      if (outputFileAndOutputRenamingMapUpToDate(previousBuildSettings,
          currentBuildSettings)) {
        skipCompilation = true;
        log("Output file \"" + cssOutputFile.getName() + "\" up-to-date. "
            + "Stylesheet compilation skipped.");
      }
    }

    if (!skipCompilation) {
      String sheetOrSheets = (cssCurrentSources.size() > 1) ? "stylesheets"
          : "stylesheet";
      log("Compiling " + cssCurrentSources.size() + " " + sheetOrSheets
          + "...");

      JobDescription compileJob = createStylesheetCompilerJobDescription(
          cssCurrentSources);
      String compiledCSS = compileStylesheets(compileJob,
          this.outputRenamingMap, this.outputRenamingMapFormat);

      if (cssOutputFile == null) {
        System.out.println(compiledCSS);
      } else {
        try {
          Files.write(compiledCSS, cssOutputFile, Charsets.UTF_8);
        } catch (IOException e) {
          throw new BuildException(e);
        }
      }
    }
  }

  /**
   * Determine if the CSS output file and renaming map are up to date. See
   * {@link ClosureBuildUtil#outputFileUpToDate(java.io.File, BuildSettings, BuildSettings)}
   *
   * @param previousSettings the settings from the previous build
   * @param currentSettings the settings for the current build
   * @return {@code true} if the CSS output file and renaming map are up-to-date
   */
  private boolean outputFileAndOutputRenamingMapUpToDate(
      @Nullable BuildSettings previousSettings, BuildSettings currentSettings) {

    if (this.outputFile != null && previousSettings != null) {
      if (ClosureBuildUtil.outputFileUpToDate(new File(this.outputFile),
          previousSettings, currentSettings)) {
        if (this.outputRenamingMap != null) {
          if (ClosureBuildUtil.outputFileUpToDate(
              new File(this.outputRenamingMap), previousSettings,
              currentSettings)) {
            return true;
          }
        } else {
          return true;
        }
      }
    }
    return false;
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
      try {
        currentBuildSources.addAll(Files.readLines(
            new File(this.inputManifest), Charsets.UTF_8));
      } catch (IOException e) {
        throw new BuildException(e);
      }
    }
    currentBuildSources.addAll(AntUtil
        .getFilePathsFromCollectionOfFileSet(getProject(), this.cssFileSets));

    return currentBuildSources;
  }

  /**
   * Run the Closure Stylesheets compiler and return the compiled CSS as a
   * string. If a non-null renaming map file path is specified, then the
   * renaming file will be written as well.
   *
   * @param job Closure Stylesheets compiler job description
   * @param renamingMapFilePath optional file path to write the CSS renaming
   *     map
   * @param renamingMapFormat optional CSS renaming map format. Only used if
   *     if a renaming map file path is specified.
   * @return the compiled CSS
   */
  private String compileStylesheets(JobDescription job,
      @Nullable String renamingMapFilePath,
      @Nullable OutputRenamingMapFormat renamingMapFormat) {

    ClosureStylesheetsCompiler compiler = new ClosureStylesheetsCompiler(job,
        new ExitCodeHandler() {
          @Override public void processExitCode(int i) {
            throw new BuildException("<" + getTaskName() + "> finished with "
                + "exit code " + i);
          }
        },
        new BasicErrorManager() {
          @Override public void print(String s) {
            log(s, Project.MSG_ERR);
          }
        }
    );

    File renamingMapFile = null;

    if (renamingMapFilePath != null) {
      renamingMapFile = new File(renamingMapFilePath);
      try {
        renamingMapFile.getParentFile().mkdirs();
        renamingMapFile.createNewFile();
      } catch (IOException e) {
        throw new BuildException("unable to create CSS renaming map file: "
            + renamingMapFilePath, e);
      } catch (SecurityException se) {
        throw new BuildException(se);
      }
    }

    if (renamingMapFormat != null) {
      compiler.setOutputRenamingMapFormat(renamingMapFormat);
    }

   return compiler.execute(renamingMapFile);
  }

  /**
   * Create a {@link JobDescription} for the Closure Stylesheets compiler
   * initialized with data set for the {@link ClosureStylesheetsTask}.
   *
   * @param cssSources file paths for CSS sources to be compiled
   * @return a job description for a run of the Closure Stylesheets compiler
   * @throws BuildException if there is an {@link IOException} reading one of
   *     the CSS source files
   */
  private JobDescription createStylesheetCompilerJobDescription(
      List<String> cssSources) {
    JobDescriptionBuilder jobBuilder = new JobDescriptionBuilder();

    jobBuilder.setInputOrientation(this.inputOrientation);
    jobBuilder.setOutputOrientation(this.outputOrientation);
    jobBuilder.setOutputFormat(this.prettyPrint
        ? JobDescription.OutputFormat.PRETTY_PRINTED
        : JobDescription.OutputFormat.COMPRESSED);
    jobBuilder.setCopyrightNotice(this.copyrightNotice);
    jobBuilder.setTrueConditionNames(this.definedTrueConditionals);
    jobBuilder.setAllowUnrecognizedFunctions(this.allowUnrecognizedFunctions);
    jobBuilder.setAllowedNonStandardFunctions(this.allowedNonStandardFunctions);
    jobBuilder.setAllowWebkitKeyframes(true);
    jobBuilder.setProcessDependencies(true);
    jobBuilder.setExcludedClassesFromRenaming(this.classesExcludedFromRenaming);
    jobBuilder.setSimplifyCss(true);
    jobBuilder.setEliminateDeadStyles(true);
    jobBuilder.setCssSubstitutionMapProvider(
        this.renamingType.getCssSubstitutionMapProvider());

    GssFunctionMapProvider gssFunctionMapProvider =
        createGssFunctionMapProviderForName(
            this.gssFunctionMapProviderClassName,
            this.gssFunctionMapProviderClasspath);
    jobBuilder.setGssFunctionMapProvider(gssFunctionMapProvider);

    for (String filePath : cssSources) {
      File file = new File(filePath);
      if (!file.exists()) {
        throw new BuildException(String.format(
            "Input file %s does not exist", filePath));
      }

      String fileContents;

      try {
        fileContents = Files.toString(file, Charsets.UTF_8);
      } catch (IOException e) {
        throw new BuildException(e);
      }
      jobBuilder.addInput(new SourceCode(filePath, fileContents));
    }

    return jobBuilder.getJobDescription();
  }

  /**
   * Create a new instance of the named class, which must implement {@link
   * GssFunctionMapProvider}.An optional classpath may be specified,
   * which will be searched for the class.
   *
   * @param className optional fully qualified Java class name such as
   *     "com.google.common.css.compiler.gssfunctions.DefaultGssFunctionMapProvider"
   * @param classpath optional {@link Path} containing classpaths to search
   *     for {@code className}
   * @return a new instance of the {@link GssFunctionMapProvider} that
   *     corresponds to the specified class name, or a new instance of
   *     {@link DefaultGssFunctionMapProvider} if the class name is
   *     {@code null}.
   * @throws RuntimeException if the classname cannot be found, accessed, or
   *     instantiated
   */
  private GssFunctionMapProvider createGssFunctionMapProviderForName(
      @Nullable String className, @Nullable Path classpath) {
    if (Strings.isNullOrEmpty(className)) {
      return new DefaultGssFunctionMapProvider();
    }

    ClassLoader classLoader = null;

    if (classpath != null && !classpath.toString().isEmpty()) {
      classLoader = new AntClassLoader(
          this.getClass().getClassLoader(), getProject(), classpath, true);
    }

    try {
      if (classLoader == null) {
        return (GssFunctionMapProvider) Class.forName(className).newInstance();
      } else {
        return (GssFunctionMapProvider) Class.forName(className, true,
            classLoader).newInstance();
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot find class: " + className, e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Cannot access class: " + className, e);
    } catch (InstantiationException e) {
      throw new RuntimeException("Cannot instantiate class: " + className, e);
    }
  }

  /**
   * Get the attribute and nested element settings for this Ant task. Each
   * attribute-value pair is formatted as: {@literal <attribute>=<value>}. CSS
   * input files are not included. See {@link #getAllSources()}.
   *
   * <p>This method serves to compare the settings between invocations of this
   * Ant task so that execution may be skipped if nothing has changed.</p>
   *
   * @return a string containing the attribute and nested element settings for
   *     this Ant task
   */
  private String getAntTaskSettingsExcludingSourcesAsString() {
    StringBuilder builder = new StringBuilder();

    builder.append("allowUnrecognizedFunctions=")
        .append(this.allowUnrecognizedFunctions);
    builder.append(" copyrightNotice=").append(this.copyrightNotice);
    builder.append(" forceRecompile=").append(this.forceRecompile);
    builder.append(" functionMapProvider=")
        .append(this.gssFunctionMapProviderClassName);
    builder.append(" classpath=").append(this.gssFunctionMapProviderClasspath);
    builder.append(" inputOrientation=")
        .append(this.inputOrientation.toString());
    builder.append(" outputFile=").append(this.outputFile);
    builder.append(" outputOrientation=")
        .append(this.outputOrientation.toString());
    builder.append(" outputRenamingMap=").append(this.outputRenamingMap);
    builder.append(" outputRenamingMapFormat=")
        .append(this.outputRenamingMapFormat.toString());
    builder.append(" prettyPrint=").append(this.prettyPrint);
    builder.append(" renamingType").append(this.renamingType.toString());

    for (String nonStndFunction : this.allowedNonStandardFunctions) {
      builder.append(" allowedNonStandardFunction=").append(nonStndFunction);
    }
    for (String excludedClass : this.classesExcludedFromRenaming) {
      builder.append(" classExcludedFromRenaming=").append(excludedClass);
    }
    for (String trueConditional : this.definedTrueConditionals) {
      builder.append(" definedTrueConditional=").append(trueConditional);
    }

    return builder.toString();
  }
}
