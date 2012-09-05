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

package org.closureant.plovr;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.FileSet;

import org.closureant.util.AntUtil;

/**
 * Data type for {@literal <module>} nested element of {@link org.closureant.Plovr} Ant task.
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
 * <tr class="altColor"><td><b>name</b></td><td>The name of the module.</td>
 *     <td><b>Yes</b>.</td></tr>
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
 * <tr class="altColor"><td><b>dep</b></td><td>Specifies a module
 *     that this module depends on.</td></tr>
 * <tr class="rowColor"><td><b>inputs</b></td><td>Input files that must be
 *     contained in this module. The {@literal <inputs>} element is an
 *     Ant <a href="http://ant.apache.org/manual/Types/fileset.html">FileSet
 *     </a> (i.e. it supports FileSet's attributes and nested elements).</td>
 *     </tr>
 * </tbody>
 * </table>
 * </li>
 * </ul>
 *
 *
 * <ul class="blockList">
 * <li class="blockList">
 * <h3>Example</h3>
 *
 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0">
 * <col width="100%"/>
 * <thead>
 * <tr><th>Based on the plovr <a target="_blank"
 * href="http://code.google.com/p/plovr/source/browse/#hg%2Ftestdata%2Fmodules">
 * module test data</a>.</th></tr>
 * </thead>
 * <tbody>
 * <tr class="rowColor"><td>
 *
<p><pre>{@literal
<plovr configID="module-example"
    configFile="plovr-config.json"
    plovrMode="build"
    moduleOutputPath="${build.dir}/module_%s.js"
    moduleProductionURI="${build.dir}/module_%s.js"
    globalScopeName="__plovr__">
  <compiler compilationLevel="advanced" warningLevel="VERBOSE" />
  <module name="app">
    <inputs includes="app*.js" dir="." />
  </module>
  <module name="api">
    <inputs includes="api*.js" dir="." />
    <dep module="app" />
  </module>
  <module name="settings">
    <inputs includes="settings*.js" dir="." />
    <dep module="app" />
  </module>
</plovr>
}</pre></p>
 * </td></tr>
 * </tbody>
 * </table>
 * </li>
 * </ul>
 *
 * <p>See: <a target="_blank" href="http://plovr.com/options.html#modules">
 * plovr {@code modules} option</a></p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class Module extends DataType {

  // Attributes

  private String name;
  
  // Nested Elements

  private final List<ModuleDep> deps;
  private final List<FileSet> inputs;

  public Module() {
    this.deps = Lists.newArrayList();
    this.inputs = Lists.newArrayList();    
  }

  /**
   * @param name the module name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the module name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Add the name of a module that this module depends on.
   *
   * @param dep a module dependency
   */
  public void addDep(ModuleDep dep) {
    this.deps.add(dep);
  }

  /**
   * JavaScript source files that will be contained in this module.
   *
   * @param inputFiles a JavaScript file
   */
  public void addInputs(FileSet inputFiles) {
    this.inputs.add(inputFiles);
  }

  /**
   * @return a list of module input files
   */
  public List<String> getInputs() {
    List<String> inputList = Lists.newArrayList();

    for (FileSet input : inputs) {
      List<File> inputFiles = 
          AntUtil.getListOfFilesFromAntFileSet(getProject(), input);
      for (File inputFile : inputFiles) {
        inputList.add(inputFile.getAbsolutePath());
      }
    }

    return inputList;
  }

  /**
   * @return a list of module dependencies
   */
  public List<String> getDeps() {
    List<String> depList = Lists.newArrayList();

    for (ModuleDep dep : deps) {
      depList.add(dep.getModule());
    }
    return depList;
  }
}