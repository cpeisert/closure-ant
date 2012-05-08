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

package org.builderplus;

/**
 * Output modes supported by Builder Plus.
 *
 * @author cpeisert@gmail.com (Christopher Peisert)
 */
public enum OutputMode {

  /**
   * Produces compiled output with the Closure Compiler.
   */
  COMPILED,

  /**
   * Produces a manifest suitable for the Closure Compiler. Such a manifest
   * is an ordered list of JavaScript source files derived from the
   * transitive dependencies of the program entry points.
   */
  MANIFEST,

  /**
   * Produces a single script containing the concatenated contents of all
   * the files.
   */
  RAW,
  ;
}