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

import org.apache.tools.ant.Project;

/**
 * Enumeration constants corresponding to the Ant properties defined in
 * "closure-ant-tasks.xml".
 *
 * @author cpeisert@gmail.com (Christopher Peisert)
 */
public enum SharedAntProperty {
  /**
   * The root directory where the Closure Tools are stored on the local
   * machine.
   */
  CLOSURE_TOOLS_DIR("closure-tools.dir"),
  /** The root directory of the Closure Library. */
  CLOSURE_LIBRARY_DIR("closure-library.dir"),
  /** The directory where the Closure Builder python scripts are located. */
  CLOSURE_BUILDER_DIR("closure-builder.dir"),
  /**
   * The file path to closurebuilder.py, which is the entry point for Closure
   * Builder.
   */
  CLOSURE_BUILDER_PY("closurebuilder.py"),
  /** The file path to depswriter.py. */
  DEPS_WRITER_PY("depswriter.py"),
  /** The name of Closure Linter executable. Defaults to "gjslint". */
  CLOSURE_LINTER_PY("closure-linter.py"),
  /** The directory where the Closure Compiler is located. */
  CLOSURE_COMPILER_DIR("closure-compiler.dir"),
  /** The file path to the Closure Compiler jar file. */
  CLOSURE_COMPILER_JAR("closure-compiler.jar"),
  /** The directory where Closure Stylesheets is located. */
  CLOSURE_STYLESHEETS_DIR("closure-stylesheets.dir"),
  /** The file path to the Closure Stylesheets jar file. */
  CLOSURE_STYLESHEETS_JAR("closure-stylesheet.jar"),
  /** The directory where Closure Templates is located. */
  CLOSURE_TEMPLATES_DIR("closure-templates.dir"),
  /** The file path to the Closure Templates jar file. */
  CLOSURE_TEMPLATES_JAR("closure-templates.jar"),
  /**
   * The file path to soyutils.js, which is a component of Closure Templates.
   */
  SOYUTILS_JS("soyutils.js"),
  /**
   * The file path to soyutils_usegoog.js, which is a component of Closure
   * Templates.
   */
  SOYUTILS_USEGOOG_JS("soyutils_usegoog.js"),
  /** The directory where plovr is located. */
  PLOVR_DIR("plovr.dir"),
  /** The file path to the plovr jar file. */
  PLOVR_JAR("plovr.jar"),
  ;

  private final String name;
  
  private SharedAntProperty(String name) {
    this.name = name;
  }

  /**
   * Get the name of the Ant property.
   *
   * @return the Ant property name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Look up the value for the property in the Ant project.
   *
   * @param project the Ant project
   * @return the value of the property or {@code null} if not found or not set
   */
  public String getValue(Project project) {
    return project.getProperty(this.name);
  }
}
