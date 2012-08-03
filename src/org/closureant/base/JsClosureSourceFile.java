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

import java.util.Collection;

/**
 * A JavaScript source file that follows the Google Closure Library
 * dependency management convention of using {@code goog.provide()} and
 * {goog.require()}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public interface JsClosureSourceFile extends ProvidesRequiresSourceFile {

  /** The JavaScript source code. */
  String getCode();

  /** The set of namespaces goog.provided by this source file. */
  Collection<String> getProvides();

  /** The set of namespaces goog.required by this source file. */
  Collection<String> getRequires();

  /** If this input is the Closure Library's base.js. */
  boolean isBaseJs();
}