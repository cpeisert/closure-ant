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

package org.closureant.base;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import org.closureant.types.CompileTimeDefines;
import org.closureant.types.ExperimentalCompilerOptions;
import org.closureant.types.PlovrOutputModuleCollection;

/**
 * TODO(cpeisert): add support for test-drivers config option
 *
 * Object representing a plovr configuration to serialize to a plovr JSON
 * config file. This class only exists to generate plovr config files
 * and is not intended for general use.
 *
 * <p><b>NOTE:</b> The order of the members matches the <a target="_blank"
 * href="http://plovr.com/options.html">plovr config options</a> as shown
 * in the <a href="#field_detail"><b>Field Detail</b></a> below.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class PlovrConfig {
  
  public String id;
  
  public List<String> inputs;

  public List<String> paths;

  public List<String> externs;

  @SerializedName("custom-externs-only")
  public Boolean customExternsOnly;

  @SerializedName("closure-library")
  public String closureLibrary;

  @SerializedName("experimental-exclude-closure-library")
  public Boolean experimentalExcludeClosureLibrary;

  public String mode;
  
  public String level;
  
  public String inherits;

  public Boolean debug;

  @SerializedName("pretty-print")
  public Boolean prettyPrint;

  @SerializedName("print-input-delimiter")
  public Boolean printInputDelimiter;

  @SerializedName("output-file")
  public String outputFile;

  @SerializedName("output-wrapper")
  public List<String> outputWrapper;
  
  @SerializedName("ouput-charset")
  public String outputCharset;

  public Boolean fingerprint;

  public PlovrOutputModuleCollection modules;

  @SerializedName("module-output-path")
  public String moduleOutputPath;

  @SerializedName("module-production-uri")
  public String moduleProductionURI;
  
  @SerializedName("module-info-path")
  public String moduleInfoPath;

  @SerializedName("global-scope-name")
  public String globalScopeName;

  public CompileTimeDefines define;

  public Map<String, String> checks;

  @SerializedName("treat-warnings-as-errors")
  public Boolean treatWarningsAsErrors;

  @SerializedName("export-test-functions")
  public Boolean exportTestFunctions;

  @SerializedName("name-suffixes-to-strip")
  public List<String> nameSuffixesToStrip;

  @SerializedName("type-prefixes-to-strip")
  public List<String> typePrefixesToStrip;
  
  @SerializedName("id-generators")
  public List<String> idGenerators;

  @SerializedName("ambiguate-properties")
  public Boolean ambiguateProperties;

  @SerializedName("disambiguate-properties")
  public Boolean disambiguateProperties;

  @SerializedName("experimental-compiler-options")
  public ExperimentalCompilerOptions experimentalCompilerOptions;

  // TODO(cpeisert): Consider adding support for custom-passes.
  // public ListMultimap<CustomPassExecutionTime, CompilerPassFactory> customPasses;
  
  @SerializedName("soy-function-plugins")
  public List<String> soyFunctionPlugins;

  @SerializedName("jsdoc-html-output-path")
  public String jsdocHtmlOutputPath;

  @SerializedName("variable-map-input-file")
  public String variableMapInputFile;

  @SerializedName("variable-map-output-file")
  public String variableMapOutputFile;

  @SerializedName("property-map-input-file")
  public String propertyMapInputFile;

  @SerializedName("property-map-output-file")
  public String propertyMapOutputFile;

  @SerializedName("test-template")
  public String testTemplate;

  @SerializedName("test-excludes")
  public List<String> testExcludes;

  /**
   * Constructs a PlovrConfig object.
   */
  public PlovrConfig() {
    this.id = null;
    this.inputs = Lists.newArrayList();
    this.paths = Lists.newArrayList();
    this.externs = Lists.newArrayList();
    this.customExternsOnly = null;
    this.closureLibrary = null;
    this.experimentalExcludeClosureLibrary = null;
    this.mode = null;
    this.level = null;
    this.inherits = null;
    this.debug = null;
    this.prettyPrint = null;
    this.printInputDelimiter = null;
    this.outputFile = null;
    this.outputWrapper = Lists.newArrayList();
    this.outputCharset = null;
    this.fingerprint = null;
    this.modules = new PlovrOutputModuleCollection();
    this.moduleOutputPath = null;
    this.moduleProductionURI = null;
    this.moduleInfoPath = null;
    this.globalScopeName = null;
    this.define = new CompileTimeDefines();
    this.checks = Maps.newHashMap();
    this.treatWarningsAsErrors = null;
    this.exportTestFunctions = null;
    this.nameSuffixesToStrip = Lists.newArrayList();
    this.typePrefixesToStrip = Lists.newArrayList();
    this.idGenerators = Lists.newArrayList();
    this.ambiguateProperties = null;
    this.disambiguateProperties = null;
    this.experimentalCompilerOptions = new ExperimentalCompilerOptions();
    this.soyFunctionPlugins = Lists.newArrayList();
    this.jsdocHtmlOutputPath = null;
    this.variableMapInputFile = null;
    this.variableMapOutputFile = null;
    this.propertyMapInputFile = null;
    this.propertyMapOutputFile = null;
    this.testTemplate = null;
    this.testExcludes = Lists.newArrayList();
  }

  /**
   * Convenience method to set all empty member collections to {@code null}
   * to ensure that they are not added to the plovr config file.
   */
  public void nullifyEmptyCollections() {
    if (this.inputs.isEmpty()) this.inputs = null;
    if (this.paths.isEmpty()) this.paths = null;
    if (this.externs.isEmpty()) this.externs = null;
    if (this.outputWrapper.isEmpty()) this.outputWrapper = null;
    if (this.modules.isEmpty()) this.modules = null;
    if (this.define.isEmpty()) this.define = null;
    if (this.checks.isEmpty()) this.checks = null;
    if (this.nameSuffixesToStrip.isEmpty()) this.nameSuffixesToStrip = null;
    if (this.typePrefixesToStrip.isEmpty()) this.typePrefixesToStrip = null;
    if (this.idGenerators.isEmpty()) this.idGenerators = null;
    if (this.experimentalCompilerOptions.isEmpty()) 
      this.experimentalCompilerOptions = null;
    if (this.soyFunctionPlugins.isEmpty()) this.soyFunctionPlugins = null;
    if (this.testExcludes.isEmpty()) this.testExcludes = null;
  }
}