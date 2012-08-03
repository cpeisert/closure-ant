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

package org.closureant.deps;

/**
 * Represents a circular dependency exception, which occurs when a cycle is
 * formed by the dependency graph of source files based on their {@code
 * goog.provided} and {@code goog.required} namespaces.
 *
 * <p>To create a new CircularDependencyException, use {@link
 * CircularDependencyExceptionFactory#newCircularDependencyException(java.util.Collection, java.util.Collection)}
 * </p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public abstract class CircularDependencyException extends RuntimeException {

  protected CircularDependencyException(String message) {
    super(message);
  }
}