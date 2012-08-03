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
 * A source file that follows some arbitrary convention to manage
 * dependencies, such that the file "provides" named modules (for example,
 * packages, namespaces, classes, or objects) and "requires" named modules
 * that are defined in other files.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public interface ProvidesRequiresSourceFile extends SourceFile {

  /** The set of named modules provided/defined by this source file. */
  Collection<String> getProvides();

  /**
   * The set of named modules required by this source file that are defined
   * in other source files.
   */
  Collection<String> getRequires();
}