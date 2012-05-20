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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;

import org.closureextensions.ant.types.ClassNameList;
import org.closureextensions.ant.types.CompilerOptionsFactory;
import org.closureextensions.ant.types.CompilerOptionsForPlovr;
import org.closureextensions.ant.types.CompileTimeDefines;
import org.closureextensions.ant.types.Directory;
import org.closureextensions.ant.types.ExperimentalCompilerOptions;
import org.closureextensions.ant.types.PlovrOutputModule;
import org.closureextensions.ant.types.PlovrOutputModuleCollection;
import org.closureextensions.ant.types.StringNestedElement;
import org.closureextensions.common.util.AntUtil;
import org.closureextensions.common.util.ClosureBuildUtil;
import org.closureextensions.common.util.FileUtil;

/**
 * TODO(cpeisert): Add support for test-drivers config option
 *
 * TODO(cpeisert): check if Ilia's XTB translation options have been added to
 * TODO            plovr (Config-options: "translations", "language")
 * TODO            See: http://codereview.appspot.com/6194048
 *
 * TODO(cpeisert): check if Ilia's support for xliff format has been accepted
 * TODO            See: http://codereview.appspot.com/6201051
 * TODO            Need support to specify the output message file format
 * TODO            (new plovr CLI flag --format takes xliff or xtb)
 *
 * Ant task for <a target="_blank" href="http://plovr.com">plovr: a Closure
 * build tool</a>. The default task name is {@code plovr} as defined in
 * "task-definitions.xml".
 *
 * <p>The location of the plovr jar file is also defined in
 * "closure-tools-config.xml", which should be included in your build file as
 * follows:</p>
 *
 * <p>{@literal <import file="your/path/to/closure-tools-config.xml" />}</p>
 *
 * <p><i>Verify that the paths defined in "closure-tools-config.xml" are
 * correct for your local configuration.</i></p>
 *
 * <p>For more information about {@code plovr}, visit
 * <a target="_blank" href="http://plovr.com">plovr.com</a>.</p>
 *
 *
 *
 * TODO(cpeisert): Move the Ant-style documentation below into separate doc
 * files.
 *
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
 * <tr class="altColor"><td id="closureLibrary"><b>closureLibrary</b></td><td>
 *     Path to the version of the Closure Library that should be used instead
 *     of the version of the Closure Library that is bundled with plovr. The
 *     plovr documentation for <a target="_blank"
 *     href="http://plovr.com/options.html#closure-library">closure-library</a>
 *     indicates that it should identify the root directory that contains <a
 *     target="_blank" href="http://code.google.com/p/closure-library/source/browse/trunk/closure/goog/base.js">
 *     base.js</a>. However, when using the plovr Ant task, any parent 
 *     directory of the root containing base.js may be used since this Ant task
 *     recursively scans the provided directory until it finds base.js. The
 *     parent directory of base.js is used in the generated plovr config file.
 *
 *     <p>For example, if the top level "closure-library" directory is provide,
 *     then "closure-library/closure/goog/" is used in the config file.</p>
 *     </td><td>No.</td></tr>
 * <tr class="rowColor"><td id="configID"><b>configID</b></td><td>A plovr
 *     config ID. Every config must have an ID. The ID must be unique among the
 *     configs being served by plovr because the ID is a parameter to every
 *     function in the plovr REST API.</td><td><b>Yes</b>.</td></tr>
 * <tr class="altColor"><td id="configFile"><b>configFile</b></td><td>The
 *     plovr config file. In plovr modes {@code BUILD} and {@code CONFIG} the
 *     config file is rewritten based on the task attributes and nested element
 *     settings.</td><td>No.</td></tr>
 * <tr class="rowColor"><td id="experimentalExcludeClosureLibrary">
 *     <b>experimentalExcludeClosureLibrary</b></td><td>This is an experimental
 *     option to address <a target="_blank"
 *     href="http://code.google.com/p/plovr/issues/detail?id=40">Issue 40</a>.
 *     When set to {@code true}, it will exclude Closure Library's
 *     {@code base.js} from the compiled output. This is primarily for
 *     developers using a library other than Closure, such as jQuery. Because
 *     neither {@code goog.require()} nor {@code goog.provide()} will be defined
 *     without {@code base.js}, all inputs to compilation will have to be 
 *     listed explicitly, in order, as part of the {@literal <inputs>} nested
 *     element.</td><td>No.</td></tr>
 * <tr class="altColor"><td id="exportTestFunctions"><b>exportTestFunctions
 *     </b></td><td>When the JavaScript source is compiled, all global
 *     functions that start with {@code test} will be exported via
 *     {@code goog.exportSymbol()} so that when run as part of the Closure
 *     testing framework, the test methods will still be able to be discovered.
 *     In short, this makes it possible to unit test JavaScript code compiled
 *     in Advanced mode.</td><td>No.</td></tr>
 * <tr class="rowColor"><td id="fingerprint"><b>fingerprint</b></td><td>
 *     Determines whether to fingerprint the JS files for modules when plovr
 *     is used in build mode. The fingerprint is an md5 hash of the file
 *     content. Defaults to {@code false}.</td><td>No.</td></tr>
 * <tr class="altColor"><td id="globalScopeName"><b>globalScopeName</b></td>
 *     <td>A scope name for multiple modules to share. This will cause
 *     modules to be wrapped with a
 *
 * <p><pre>
 * (function(a) { with (a) { ... } })(scope)
 * </pre></p>
 *
 *     <p>(and the main module will additionally start with {@code scope = {}} 
 *     so that you don't actually have to do anything special). As a result, 
 *     none of the modules' global variables will make it to the real global 
 *     scope. Instead they will be inside of the specified scope variable, 
 *     which should not be touched by any other code on the page.</p></td>
 *     <td>No.</td></tr>
 * <tr class="rowColor"><td id="inherits"><b>inherits</b></td><td>Config file
 *     from which to inherit. When compiling multiple JavaScript files for the
 *     same project, you are likely to have common settings across your plovr
 *     config files. For this reason, it is possible for one config file to
 *     "inherit" from another config file.</td><td>No.</td></tr>
 * <tr class="altColor"><td id="jsdocHtmlOutputPath"><b>jsdocHtmlOutputPath
 *     </b></td><td>The path where jsdoc html files should be written when
 *     invoking plovr in {@code jsdoc} mode.</td><td>No.</td></tr>
 * <tr class="rowColor"><td id="moduleInfoPath"><b>moduleInfoPath</b></td><td>
 *     This option is used to write the plovr module info JS into a separate
 *     file instead of prepending it to the root module. Prepending the JS
 *     causes the source map to be several lines off in the root module, so
 *     doing this avoids that issue.
 *     
 *     <p>See <a target="_blank"
 *     href="http://code.google.com/p/plovr/issues/detail?id=50">plovr issue 50
 *     </a>.</p></td><td>No.</td></tr>
 * <tr class="altColor"><td id="moduleOutputPath"><b>moduleOutputPath</b></td>
 *     <td>The file path where compiled modules are written. The placeholder
 *     for the module name is {@code %s}, for example
 *     {@code ../build/module_%s.js}.
 *
 *     <p>See module example <a target="_blank" 
 *     href="http://code.google.com/p/plovr/source/browse/testdata/modules/plovr-config.js?spec=svna9bb5aa2b557bdde06d327f6b30f2e8006516589&r=4edc58e190ad2d80993fda3cd9d4f941586e1aa7">
 *     plovr-config.js</a>.</p></td><td>No.</td></tr>     
 * <tr class="rowColor"><td id="moduleProductionURI"><b>moduleProductionURI
 *     </b></td><td>The URI (path) for modules compiled for production.
 *
 *     <p>See module example <a target="_blank" 
 *     href="http://code.google.com/p/plovr/source/browse/testdata/modules/plovr-config.js?spec=svna9bb5aa2b557bdde06d327f6b30f2e8006516589&r=4edc58e190ad2d80993fda3cd9d4f941586e1aa7">
 *     plovr-config.js</a>.</p></td><td>No.</td></tr> 
 * <tr class="altColor"><td id="outputFile"><b>outputFile</b></td><td>The file
 *     to write output to instead of standard output. Note that if modules are
 *     used, you must use {@code moduleOutputPath} instead.</td><td>No.</td>
 *     </tr>
 * <tr class="rowColor"><td id="plovrJar"><b>plovrJar</b></td><td>The plovr
 *     jar file to execute.</td><td>No, as long as your build file imports
 *     closureextensions.xml.</td></tr>
 * <tr class="altColor"><td id="plovrMode"><b>plovrMode</b></td><td>The plovr
 *     execution mode. Options: "build", "config", "jsdoc", or "serve". See
 *     {@link PlovrMode}</td><td>No.</td></tr>
 * <tr class="rowColor"><td id="serverListenAddress"><b>serverListenAddress
 *     </b></td><td>The IP address on which the plovr server will listen.
 *     Defaults to "0" but could also be "localhost".</td><td>No.</td></tr>
 * <tr class="altColor"><td id="serverPort"><b>serverPort</b></td><td>The
 *     port on which to run the plovr server.</td><td>No.</td></tr>
 * <tr class="rowColor"><td id="testTemplate"><b>testTemplate</b></td><td>The
 *     Soy file to use as a template for JsUnit-style tests. The template
 *     will receive the following parameters:
 *
 *     <p><ul>
 *     <li>title</li>
 *     <li>baseJsUrl</li>
 *     <li>testJsUrl</li>
 *     </ul></p></td><td>No.</td></tr>
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
 *
 * <tr class="altColor"><td><b>compiler</b></td><td>Options for the Closure 
 *     Compiler. For documentation see
 *     {@link org.closureextensions.ant.types.PlovrClosureCompiler}</td></tr>
 * <tr class="rowColor"><td><b>inputs</b></td><td>Input files to be
 *     compiled. Each input file and its transitive dependencies will be
 *     included in the compiled output. The {@literal <inputs>} element is an
 *     Ant <a href="http://ant.apache.org/manual/Types/fileset.html">FileSet
 *     </a> (i.e. it supports FileSet's attributes and nested elements).</td>
 *     </tr>
 * <tr class="altColor"><td><b>module</b></td><td>See {@link PlovrOutputModule}
 *     </td></tr>
 * <tr class="rowColor"><td><b>path</b></td><td>A path to be recursively
 *     traversed to build the dependencies.</td></tr>
 * <tr class="altColor"><td><b>sources</b></td><td>Source files available
 *     to the build process that will be used if they are transitively
 *     required by one of the {@code namespaces} or {@code inputs}. The
 *     {@literal <sources>} element is an Ant <a
 *     href="http://ant.apache.org/manual/Types/fileset.html">FileSet
 *     </a> (i.e. it supports FileSet's attributes and nested elements).</td>
 *     </tr>
 * <tr class="rowColor"><td><b>soyFunctionPlugin</b></td><td>Specifies the
 *     full class names of Guice modules for Soy (Closure Templates) function
 *     plugins and print directive plugins. For example,
 *     {@code org.plovr.soy.function.PlovrModule} defines the Soy function
 *     {@code substring()}, so to use that function in your Closure Templates,
 *     include this option in your {@literal <plovr>} Ant task:
 *
 * <p><pre>{@literal
 * <soyFunctionPlugins value="org.plovr.soy.function.PlovrModule" />
 * }</pre></p></td></tr>
 * <tr class="altColor"><td><b>testExcludes</b></td><td>By default, all files
 *     that end in {@code _test.js} under the {@literal <paths>} directories
 *     are included in the test runner that runs all of the JS unit tests.
 *     This option can be used to specify subpaths of {@literal <paths>} that
 *     should be excluded from testing.</td>
 *     </tr>
 * </tbody>
 * </table>
 * </li>
 * </ul>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class PlovrTask extends Task {

  private static final String PLOVR_SERVER_ADDRESS = "0";
  private static final int PLOVR_SERVER_PORT = 9810;

  /** 
   * Execution modes supported by plovr. Note: "soyweb" is available as the
   * standalone Ant task {@link PlovrSoyWebTask}.
   */
  public static enum PlovrMode {
    /**
     * Produces compiled JavaScript.
     *
     * <p>Build mode reads Closure Templates (.soy files) and JavaScript files
     * based on the supplied {@code inputs}, {@code paths}, and {@code sources}.
     * Closure Templates are first compiled to JavaScript files. Next, the
     * inputs are topologically sorted based on their dependencies. Lastly,
     * the JavaScript files are compiled together using the Closure Compiler.
     * </p>
     *
     * <p>See: <a target="_blank"
     * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
     * Definitive Guide</i></a> Appendix C. plovr</p>
     */
    BUILD,

    /**
     * Generates a plovr JSON config file and writes to standard out unless
     * {@code configFile} is specified.
     */
    CONFIG,

    /**
     * Generates the documentation for all of the source files specified by a
     * single config file.
     */
    JSDOC,

    /**
     * Starts the plovr web server. Once it is running, point your browser to
     * {@literal http://localhost:9810/compile?id=<your-plovr-config-id>}
     */
    SERVE,
    ;
  }

  // Attributes
  private String closureLibrary;
  private File configFile;
  private String configID;
  private Boolean experimentalExcludeClosureLibrary;
  private Boolean exportTestFunctions;
  private Boolean fingerprint;
  private boolean forceRecompile;
  private String globalScopeName;
  private File inherits;
  private String jsdocHtmlOutputPath;
  private String moduleInfoPath;
  private String moduleOutputPath;
  private String moduleProductionURI;
  private File outputFile;
  private File plovrJar;
  private PlovrMode plovrMode;
  private String serverListenAddress;
  private Integer serverPort;
  private File testTemplate;

  // Nested elements
  private CompilerOptionsForPlovr compilerOptions;
  private final List<FileSet> inputs;
  private final PlovrOutputModuleCollection modules;
  private final List<Directory> paths;
  private final List<FileSet> sources;
  private final List<String> soyFunctionPlugins;
  private final List<StringNestedElement> testExcludes;


  /**
   * Constructs a new Ant task for plovr.
   */
  public PlovrTask() {

    // Attributes
    this.closureLibrary = null;
    this.configFile = null;
    this.configID = null;
    this.experimentalExcludeClosureLibrary = null;
    this.exportTestFunctions = null;
    this.fingerprint = null;
    this.forceRecompile = false;
    this.globalScopeName = null;
    this.inherits = null;
    this.jsdocHtmlOutputPath = null;
    this.moduleInfoPath = null;
    this.moduleOutputPath = null;
    this.moduleProductionURI = null;
    this.outputFile = null;
    this.plovrJar = null;
    this.plovrMode = PlovrMode.BUILD;
    this.serverListenAddress = null;
    this.serverPort = null;
    this.testTemplate = null;

    // Nested elements
    this.compilerOptions = null;
    this.inputs = Lists.newArrayList();
    this.modules = new PlovrOutputModuleCollection();
    this.paths = Lists.newArrayList();    
    this.sources = Lists.newArrayList();
    this.soyFunctionPlugins = Lists.newArrayList();
    this.testExcludes = Lists.newArrayList();
  }


  // Attribute setters

  /**
   * Path to the version of the Closure Library that should be used instead of 
   * the version of the Closure Library that is bundled with plovr. The plovr
   * documentation for <a target="_blank"
   * href="http://plovr.com/options.html#closure-library">closure-library</a>
   * indicates that it should identify the root directory that contains <a
   * target="_blank" href="http://code.google.com/p/closure-library/source/browse/trunk/closure/goog/base.js">
   * base.js</a>. However, when using the plovr Ant task, any parent directory
   * of the root containing base.js may be used since this Ant task
   * recursively scans the provided directory until it finds base.js. The
   * parent directory of base.js is used in the generated plovr config file.
   *
   * <p>For example, if the top level "closure-library" directory is provide,
   * then "closure-library/closure/goog/" is used in config file.</p>
   * 
   * @param closureLibrary path to Closure Library directory containing 
   *     {@code base.js}
   */
  public void setClosureLibrary(String closureLibrary) {
    this.closureLibrary = closureLibrary;
  }

  /**
   * The plovr config file. In plovr modes {@code BUILD} and {@code CONFIG}
   * the config file is rewritten based on the task attributes and nested
   * element settings.
   *
   * @param configFile
   */
  public void setConfigFile(File configFile) {
    this.configFile = configFile;
  }

  /**
   * A plovr config ID. Every config must have an ID. The ID must be unique 
   * among the configs being served by plovr because the ID is a parameter to 
   * every function in the plovr REST API.
   * 
   * @param configID a plovr config ID
   */
  public void setConfigID(String configID) {
    this.configID = configID;
  }

  /**
   * This is an experimental option to address <a target="_blank"
   * href="http://code.google.com/p/plovr/issues/detail?id=40">Issue 40</a>.
   * When set to {@code true}, it will exclude Closure Library's
   * {@code base.js} from the compiled output. This is primarily for
   * developers using a library other than Closure, such as jQuery. Because
   * neither {@code goog.require()} nor {@code goog.provide()} will be defined
   * without {@code base.js}, all inputs to compilation will have to be listed
   * explicitly, in order, as part of the {@literal <inputs>} nested element.
   *
   * <p>See <a target="_blank"
   * href="http://plovr.com/options.html#experimental-exclude-closure-library">
   * experimental-exclude-closure-library</a> on plovr.com.</p>
   *
   * @param experimentalExcludeClosureLibrary determines if the Closure
   *     Library's {@code base.js} should be excluded from the compiled output
   */
  public void setExperimentalExcludeClosureLibrary(
      boolean experimentalExcludeClosureLibrary) {
    this.experimentalExcludeClosureLibrary = experimentalExcludeClosureLibrary;
  }

  /**
   * When the JavaScript source is compiled, all global functions that start
   * with {@code test} will be exported via {@code goog.exportSymbol()} so
   * that when run as part of the Closure testing framework, the test methods
   * will still be able to be discovered. In short, this makes it possible to
   * unit test JavaScript code compiled in Advanced mode.
   *
   * @param exportTestFunctions determines if global functions that start with 
   *     {@code test} will be exported via {@code goog.exportSymbol()}  
   */
  public void setExportTestFunctions(boolean exportTestFunctions) {
    this.exportTestFunctions = exportTestFunctions;
  }

  /**
   * @param fingerprint whether to fingerprint the JS files for modules when
   *     plovr is used in build mode. The fingerprint is an md5 hash of the
   *     filecontent. Defaults to {@code false}.
   */
  public void setFingerprint(boolean fingerprint) {
    this.fingerprint = fingerprint;
  }

  /**
   * Determines if plovr should always recompile the {@code outputFile}, even
   * if none of the input files (JavaScript, soy, coffee script) have changed
   * since the {@code outputFile} was last modified. This attribute only has
   * an effect when plovr is run in "build" mode, otherwise it is ignored.
   *
   * @param forceRecompile recompile sources even if output file is up to
   *     date. Defaults to {@code false}.
   */
  public void setForceRecompile(boolean forceRecompile) {
    this.forceRecompile = forceRecompile;
  }

  /**
   * A scope name for multiple modules to share. This will cause modules to 
   * be wrapped with a
   * 
   * <p><pre>
   * (function(a) { with (a) { ... } })(scope)
   * </pre></p>
   * 
   * <p>(and the main module will additionally start with {@code scope = {}}
   * so that you don't actually have to do anything special). As a result, none
   * of the modules' global variables will make it to the real global scope. 
   * Instead they will be inside of the specified scope variable, which should 
   * not be touched by any other code on the page.</p>
   * 
   * @param globalScopeName the name of the global scope
   */
  public void setGlobalScopeName(String globalScopeName) {
    this.globalScopeName = globalScopeName;
  }

  /**
   * Config file from which to inherit. When compiling multiple JavaScript
   * files for the same project, you are likely to have common settings across
   * your plovr config files. For this reason, it is possible for one config
   * file to "inherit" from another config file.
   *
   * <p>See <a target="_blank" href="http://plovr.com/options.html#inherits">
   * inherits</a> on plovr.com.</p>
   *
   * @param inherits a config file from which to inherit settings
   */
  public void setInherits(File inherits) {
    this.inherits = inherits;
  }

  /**
   * The path where jsdoc html files should be written when invoking plovr
   * in {@code jsdoc} mode.
   *
   * @param jsdocHtmlOutputPath the jsdoc html output path
   */
  public void setJsdocHtmlOutputPath(String jsdocHtmlOutputPath) {
    this.jsdocHtmlOutputPath = jsdocHtmlOutputPath;
  }

  /**
   * This option is used to write the plovr module info JS into a separate file
   * instead of prepending it to the root module. Prepending the JS causes the
   * source map to be several lines off in the root module, so doing this
   * avoids that issue.
   * 
   * TODO(bolinfest): A better approach may be to fix the source map, in which
   * case this option could be eliminated.
   *
   * <p>See <a target="_blank"
   * href="http://code.google.com/p/plovr/issues/detail?id=50">plovr issue 50
   * </a>.</p>
   *
   * @param moduleInfoPath file to write the plovr module info
   */
  public void setModuleInfoPath(String moduleInfoPath) {
    this.moduleInfoPath = moduleInfoPath;
  }

  /**
   * The file path where compiled modules are written. The placeholder for the
   * module name is {@code %s}, for example {@code ../build/module_%s.js}.
   *
   * <p>See module example <a target="_blank" 
   * href="http://code.google.com/p/plovr/source/browse/testdata/modules/plovr-config.js?spec=svna9bb5aa2b557bdde06d327f6b30f2e8006516589&r=4edc58e190ad2d80993fda3cd9d4f941586e1aa7">
   * plovr-config.js</a>.</p>
   *
   * @param moduleOutputPath output path for compiled modules
   */
  public void setModuleOutputPath(String moduleOutputPath) {
    this.moduleOutputPath = moduleOutputPath;
  }

  /**
   * The URI (path) for modules compiled for production. 
   * 
   * <p>See module example <a target="_blank" 
   * href="http://code.google.com/p/plovr/source/browse/testdata/modules/plovr-config.js?spec=svna9bb5aa2b557bdde06d327f6b30f2e8006516589&r=4edc58e190ad2d80993fda3cd9d4f941586e1aa7">
   * plovr-config.js</a>.</p>
   *
   * @param moduleProductionURI the URI (path) for modules compiled for 
   *     production
   */
  public void setModuleProductionURI(String moduleProductionURI) {
    this.moduleProductionURI = moduleProductionURI;
  }

  /** 
   * @param file the file to write output to instead of standard output. Note 
   *     that if modules are used, you must use {@code moduleOutputPath}
   *     instead.
   */
  public void setOutputFile(File file) {
    this.outputFile = file;
  }

  /**
   * @param plovrJar the plovr jar file to execute
   */
  public void setPlovrJar(File plovrJar) {
    this.plovrJar = plovrJar;
  }

  /**
   * @param plovrMode the plovr execution mode. Options: "build", "config",
   *     "jsdoc", "serve"
   *
   * @see PlovrTask.PlovrMode
   */
  public void setPlovrMode(String plovrMode) {
    String mode = plovrMode.toLowerCase();

    if ("build".equals(mode)
        || "config".equals(mode)
        || "jsdoc".equals(mode)
        || "serve".equals(mode)) {
      this.plovrMode = PlovrMode.valueOf(mode.toUpperCase());
    } else if ("soyweb".equals(mode)) {
      throw new BuildException("plovrMode attribute does not support "
          + "\"soyweb\". See Ant task <plovr-soyweb>.");
    } else {
      throw new BuildException("\"plovrMode\" attribute expected "
          + "to be one of \"build\", \"config\", \"jsdoc\", or \"serve\" "
          + "but was \"" + mode + "\"");
    }
  }

  /**
   * @param address the address on which to listen. Defaults to "0" but could
   *     also be "localhost"
   */
  public void setServerListenAddress(String address) {
    this.serverListenAddress = address;
  }

  /**
   * @param port the port on which to run the plovr server. Defaults to 9810.
   */
  public void setServerPort(int port) {
    this.serverPort = port;
  }
  
  /**
   * The Soy file to use as a template for JsUnit-style tests. The template
   * will receive the following parameters:
   *
   * <p><ul>
   * <li>title</li>
   * <li>baseJsUrl</li>
   * <li>testJsUrl</li>
   * </ul></p>
   *
   * <p>See <a target="_blank"
   * href="http://plovr.com/options.html#test-template">
   * test-template</a> on plovr.com.</p>
   *
   * @param testTemplate the Soy file to use as a template for JsUnit-style
   *     tests
   */
  public void setTestTemplate(File testTemplate) {
    this.testTemplate = testTemplate;
  }


  // Nested element setters

  /**
   * @return a new instance of {@link CompilerOptionsForPlovr}
   * @throws BuildException if {@literal <compiler>} nested element already
   *     used in the current plovr Ant task
   */
  public CompilerOptionsForPlovr createCompiler() {
    if (this.compilerOptions == null) {
      this.compilerOptions =
          CompilerOptionsFactory.newCompilerOptionsForPlovr();
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

  /**
   * @param module a JavaScript module to be generated by plovr and the 
   *     Closure Compiler
   */
  public void addModule(PlovrOutputModule module) {
    this.modules.add(module);
  }

  /** @param path a path to be traversed to build the dependencies */
  public void addPath(Directory path) {
    this.paths.add(path);
  }

  /**
   * Source files available to the build process that will be used if they
   * are transitively required by one of the {@code namespaces} or
   * {@code inputs}.
   *
   * @param sourceFiles source files
   */
  public void addSources(FileSet sourceFiles) {
    this.sources.add(sourceFiles);
  }

  /**
   * Specifies the full class names of Guice modules for Soy (Closure
   * Templates) function and print-directive plugins. For example,
   * {@code org.plovr.soy.function.PlovrModule} defines the Soy function
   * {@code substring()}, so to use that function in your Closure Templates,
   * include this option in your {@literal <plovr>} Ant task:
   *
   * <p><pre>{@literal
<soyfunctionplugins classNames="
    org.plovr.soy.function.PlovrModule
    org.mydomain.myapp.soy.MyPrintDirectivePlugin" />
   * }</pre></p>
   *
   * @param soyFunctionPlugins list of the full class names of Guice modules
   *     for Soy function and print-directive plugins delimited by whitespace
   *     and/or commas
   */
  public void addConfiguredSoyFunctionPlugin(ClassNameList soyFunctionPlugins) {
    this.soyFunctionPlugins.addAll(soyFunctionPlugins.getClassNames());
  }

  /**
   * By default, all files that end in {@code _test.js} under the
   * {@literal <paths>} directories are included in the test runner that runs
   * all of the JS unit tests. This option can be used to specify subpaths of
   * {@literal <paths>} that should be excluded from testing.
   *
   * <p>See <a target="_blank"
   * href="http://plovr.com/options.html#test-excludes">
   * test-excludes</a> on plovr.com.</p>
   *
   * @param path a path to exclude from {@literal <paths>}
   */
  public void addTestExcludes(StringNestedElement path) {
    this.testExcludes.add(path);
  }

  /**
   * Execute the plovr task.
   *
   * @throws org.apache.tools.ant.BuildException on error
   */
  public void execute() {

    // Verify task preconditions

    if (PlovrMode.CONFIG != this.plovrMode) {
      if (this.plovrJar == null) {
        String plovrPath = SharedAntProperty.PLOVR_JAR.getValue(getProject());
        if (plovrPath != null) {
          this.plovrJar = new File(plovrPath);
        } else {
          throw new BuildException("Required attribute \"plovrJar\" is not "
              + "set. The plovr jar file is required for plovr modes "
              + "\"build\", \"jsdoc\", and \"serve\". Verify that your build "
              + "file imports \"closure-tools-config.xml\" and that the property "
              + "locations are correct for your machine.");
        }
      }
    }
    if (Strings.isNullOrEmpty(this.configID)) {
      throw new BuildException("required attribute \"configID\" is not set");
    }

    // TODO(cpeisert): if configFile not set, create one automatically in the
    // TODO            .closure-ant-tasks directory using BuildCache
    if (this.configFile == null) {
      throw new BuildException("required attribute \"configFile\" is not set");
    }

    // Update plovr config file.

    log("Creating plovr config file \"" + this.configFile.getAbsolutePath()
        + "\"", Project.MSG_INFO);
    PlovrConfig plovrConfig = getPlovrConfigOptions();

    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(ExperimentalCompilerOptions.class,
            plovrConfig.experimentalCompilerOptions.new Serializer())
        .registerTypeAdapter(CompileTimeDefines.class,
            plovrConfig.define.new Serializer())
        .registerTypeAdapter(PlovrOutputModuleCollection.class,
            plovrConfig.modules.new Serializer())
        .create();

    // Exclude unset nested elements from appearing in the config file.
    plovrConfig.nullifyEmptyCollections();
    String currentPlovrConfig = gson.toJson(plovrConfig);
    FileUtil.write(currentPlovrConfig, this.configFile);
    String confirmation = "plovr config file successfully created: "
        + this.configFile.getAbsolutePath();
    log(confirmation, (PlovrMode.CONFIG == this.plovrMode) ? Project.MSG_INFO
        : Project.MSG_VERBOSE);

    // Run the plovr jar file.

    if (PlovrMode.CONFIG != this.plovrMode) {
      Java runner = new Java(this);
      runner.setJar(this.plovrJar);
      runner.createArg().setValue(this.plovrMode.toString().toLowerCase());
      runner.setFailonerror(true);
      runner.setFork(true);
      runner.setLogError(true);
      runner.setTaskName(getTaskName());

      if (PlovrMode.SERVE == this.plovrMode) {
        startPlovrServer(runner);
      } else if (PlovrMode.BUILD == this.plovrMode && !this.forceRecompile) {
        List<String> currentSources = getCurrentSources(plovrConfig);
        BuildCache cache = new BuildCache(this);
        BuildSettings previousBuildSettings = cache.get();
        BuildSettings currentBuildSettings = new BuildSettings(
            currentPlovrConfig, currentSources);
        cache.put(currentBuildSettings);

        boolean skipBuild = false;
        if (previousBuildSettings != null) {
          if (ClosureBuildUtil.outputFileUpToDate(this.outputFile,
              previousBuildSettings, currentBuildSettings)) {
            skipBuild = true;
            log("Output file up-to-date. Build skipped.");
          }
        }
        if (!skipBuild) {
          executePlovrJar(runner);
        }
      } else {
        executePlovrJar(runner);
      }
    }
  }

  /**
   * Creates a list of all source files (JavaScript, soy [i.e Closure
   * Templates], and coffee script) based on the inputs and paths set in a
   * {@link PlovrConfig}. Paths are recursively scanned matching all files
   * ending in ".js", ".soy", or ".coffee". Files and directories beginning
   * with a period (.) are excluded.
   *
   * See <a target="_blank" href="http://code.google.com/p/plovr/source/browse/src/org/plovr/Manifest.java">
   * Manifest.java</a> method {@literal getInputs(File file,
   * Set<JsInput> output, boolean externsOnly, final File rootOfSearch)}
   *
   * @param plovrConfig a plovr config
   * @return JavaScript, soy, and coffee script source files based on the
   *     plovr config inputs and paths
   */
  private List<String> getCurrentSources(PlovrConfig plovrConfig) {
    List<String> currentSources = Lists.newArrayList();

    if (plovrConfig.inputs != null) {
      currentSources.addAll(plovrConfig.inputs);
    }
    if (plovrConfig.paths != null) {
      for (String path : plovrConfig.paths) {
        File file = new File(path);
        if (file.isDirectory()) {
          currentSources.addAll(FileUtil.scanDirectory(file,
              /* includes */ ImmutableList.of("**/*.js", "**/*.soy", "**/*.coffee"),
              /* excludes */ ImmutableList.of(".*")));
        } else {
          currentSources.add(path);
        }
      }
    }
    return currentSources;
  }

  /**
   * Execute plovr jar file.
   *
   * @param runner the Java runner configured to execute the plovr jar file
   * @return the exit code
   * @throws BuildException if exit code does not equal zero
   */
  private int executePlovrJar(Java runner) {
    int exitCode = 0;

    // This is a hack to work around the fact that plovr does not have a
    // config file option to specify the source map output file path.
    // The plovr command line flag --create_source_map must be used.
    if (PlovrMode.BUILD == this.plovrMode && this.compilerOptions != null) {
      String sourceMapPath = this.compilerOptions.getSourceMapOutputFile();
      if (sourceMapPath != null) {
        File sourceMapOutputFile = new File(sourceMapPath);
        runner.createArg().setValue("--create_source_map");
        runner.createArg().setFile(sourceMapOutputFile);
      }
    }

    runner.createArg().setValue(this.configFile.getAbsolutePath());
    exitCode = runner.executeJava();
    if (exitCode != 0) {
      throw new BuildException("Error: " + getTaskName() + " task "
          + "finished with exit code " + exitCode);
    }
    return exitCode;
  }

  /**
   * Starts the plovr server in a new Java process.
   *
   * @param runner a Java runner configured to execute the plovr jar file
   */
  private void startPlovrServer(Java runner) {
    String plovrServerAddress = PLOVR_SERVER_ADDRESS;
    int plovrServerPort = PLOVR_SERVER_PORT;

    if (serverListenAddress != null) {
      runner.createArg().setLine("--listen " + serverListenAddress);
      plovrServerAddress = this.serverListenAddress;
    }
    if (serverPort != null) {
      runner.createArg().setLine("--port " + serverPort.toString());
      plovrServerPort = this.serverPort;
    }

    runner.createArg().setValue(this.configFile.getAbsolutePath());

    // Spawn the plovr server java process so that the Ant task exits.
    runner.setSpawn(true);
    runner.executeJava();
    log("plovr server started. Visit http://" + plovrServerAddress + ":"
        + plovrServerPort, Project.MSG_INFO);
  }

  /**
   * Initialize a {@link PlovrConfig} instance based on the Ant attribute and
   * nested element settings for this task.
   *
   * @return {@link PlovrConfig} initialized based on the task settings
   * @throws BuildException on error
   */
  private PlovrConfig getPlovrConfigOptions() {
    PlovrConfig plovrConfig;

    if (this.compilerOptions == null) {
      plovrConfig = new PlovrConfig();
    } else {
      plovrConfig = this.compilerOptions.toPlovrConfig(getProject());
    }

    // Add task attributes.

    if (this.closureLibrary != null) {
      List<String> baseJsCandidates = FileUtil.scanDirectory(
          new File(this.closureLibrary), ImmutableList.of("**/base.js"), null);

      File baseJs = null;
      for (String candidate : baseJsCandidates) {
        File file = new File(candidate);
        if (ClosureBuildUtil.isClosureBaseJs(file)) {
          baseJs = file;
          break;
        }
      }
      if (baseJs == null) {
        throw new BuildException("Error: could not find \"base.js\" in "
            + "closure library directory [" + closureLibrary + "] or any of "
            + "its subdirectories. Note: \"base.js\" must contain the line "
            + "\"var goog = goog || {}; // Identifies this file as the "
            + "Closure base.\"");
      }
      plovrConfig.closureLibrary = baseJs.getParent();
    }

    plovrConfig.id = this.configID;
    if (this.inherits != null) {
      plovrConfig.inherits = this.inherits.getAbsolutePath();
    }
    if (this.experimentalExcludeClosureLibrary != null) {
      plovrConfig.experimentalExcludeClosureLibrary =
          this.experimentalExcludeClosureLibrary;
    }
    if (this.exportTestFunctions != null) {
      plovrConfig.exportTestFunctions = this.exportTestFunctions;
    }
    if (this.fingerprint != null) {
      plovrConfig.fingerprint = this.fingerprint;
    }
    plovrConfig.globalScopeName = this.globalScopeName;
    plovrConfig.jsdocHtmlOutputPath = this.jsdocHtmlOutputPath;
    plovrConfig.moduleInfoPath = this.moduleInfoPath;
    plovrConfig.moduleOutputPath = this.moduleOutputPath;
    plovrConfig.moduleProductionURI = this.moduleProductionURI;
    if (this.outputFile != null) {
      plovrConfig.outputFile = this.outputFile.getAbsolutePath();
    }
    if (this.testTemplate != null) {
      plovrConfig.testTemplate = this.testTemplate.getAbsolutePath();
    }

    // Add nested elements. Note: <compiler> already added.

    for (FileSet input : inputs) {
      List<File> inputFiles =
          AntUtil.getListOfFilesFromAntFileSet(getProject(), input);
      for (File inputFile : inputFiles) {
        plovrConfig.inputs.add(inputFile.getAbsolutePath());
      }
    }

    for (PlovrOutputModule module : this.modules.getModules()) {
      plovrConfig.modules.add(module);
    }

    for (Directory path : paths) {
      plovrConfig.paths.add(path.getDirectory().getAbsolutePath());
    }

    for (FileSet source : sources) {
      List<File> sourceFiles =
          AntUtil.getListOfFilesFromAntFileSet(getProject(), source);
      for (File sourceFile : sourceFiles) {
        plovrConfig.paths.add(sourceFile.getAbsolutePath());
      }
    }

    for (String soyFunctionPlugin : this.soyFunctionPlugins) {
      plovrConfig.soyFunctionPlugins.add(soyFunctionPlugin);
    }

    for (StringNestedElement testExclude : this.testExcludes) {
      plovrConfig.testExcludes.add(testExclude.getValue());
    }
    return plovrConfig;
  }
}