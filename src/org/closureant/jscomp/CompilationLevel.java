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

package org.closureant.jscomp;

import com.google.common.base.Preconditions;

/**
 * Compilation levels supported by Closure Compiler.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public enum CompilationLevel {

  /**
   * Removes comments, line breaks, unnecessary spaces, and other whitespace.
   * The output JavaScript is functionally identical to the source JavaScript.
   */
  WHITESPACE_ONLY("WHITESPACE"),

  /**
   * Performs the same whitespace and comment removal as WHITESPACE_ONLY,
   * but it also performs optimizations within expressions and functions,
   * including renaming local variables and function parameters to shorter
   * names. Renaming variables to shorter names makes code significantly
   * smaller. Because the SIMPLE_OPTIMIZATIONS level renames only symbols
   * that are local to functions, it does not interfere with the interaction
   * between the compiled JavaScript and other JavaScript.
   */
  SIMPLE_OPTIMIZATIONS("SIMPLE"),

  /**
   * Performs the same transformations as SIMPLE_OPTIMIZATIONS, but adds a
   * variety of more aggressive global transformations to achieve the highest
   * compression of all three levels. To enable this extreme compression,
   * ADVANCED_OPTIMIZATIONS makes strong assumptions about the compiled code.
   * If your code does not conform to those assumptions,
   * ADVANCED_OPTIMIZATIONS will produce code that does not run.
   */
  ADVANCED_OPTIMIZATIONS("ADVANCED"),
  ;

  private final String shortName;

  private CompilationLevel(String shortName) {
    this.shortName = shortName;
  }

  /**
   * The short name of the compilation level. Will be one of: "WHITESPACE",
   * "SIMPLE", or "ADVANCED".
   *
   * @return the short name
   */
  public String shortName() {
    return this.shortName;
  }

  /**
   * Returns the CompilationLevel corresponding to the compilation level
   * {@code name}. {@code name} may be either the short name (e.g. "SIMPLE")
   * or the long name (e.g. "SIMPLE_OPTIMIZATIONS").
   *
   * @param name the name of the compilation level (not case sensitive)
   * @return the CompilationLevel corresponding to {@code name} or {@code null}
   *     if there is no matching compilation level
   * @throws NullPointerException if {@code name} is {@code null}
   */
  public static CompilationLevel fromString(String name) {
    Preconditions.checkNotNull(name, "name was null");

    for (CompilationLevel level : CompilationLevel.values()) {
      if (name.toUpperCase().startsWith(level.shortName)) {
        return level;
      }
    }
    return null;
  }
}