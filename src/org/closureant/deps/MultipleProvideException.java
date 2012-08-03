/*
 * Copyright 2010 The Closure Compiler Authors.
 * Copyright (C) 2012 Christopher Peisert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.closureant.deps;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

import org.closureant.base.ProvidesRequiresSourceFile;

/**
 * Represents an exception where a namespace is {@code goog.provided} by
 * more than one source file.
 *
 * @author nicksantos@google.com (Nick Santos)
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class MultipleProvideException extends RuntimeException {
  private static final long serialVersionUID = 0L;

  private final String providedNamespace;
  private final List<? extends ProvidesRequiresSourceFile> sourceFiles;

  /**
   * Constructs a new exception with an appropriate detail message and
   * saves the namespace that is {@code goog.provided} by multiple source
   * files.
   *
   * @param providedNamespace namespace that is {@code goog.provided} by
   *     multiple source files
   * @param source1 first source file that {@code goog.provides} the
   *     namespace
   * @param source2 second source file that {@code goog.provides} the
   *     namespace
   */
  public <E extends ProvidesRequiresSourceFile> MultipleProvideException(
      String providedNamespace, E source1, E source2) {
    this(providedNamespace, ImmutableList.of(source1, source2));
  }

  /**
   * Constructs a new exception with an appropriate detail message and
   * saves the namespace that is {@code goog.provided} by multiple source
   * files.
   *
   * @param providedNamespace namespace that is {@code goog.provided} by
   *     multiple source files
   * @param sourceFiles source files that {@code goog.provide} the namespace
   */
  public <E extends ProvidesRequiresSourceFile> MultipleProvideException(
      String providedNamespace, List<E> sourceFiles) {
    super("goog.provided namespace \"" + providedNamespace + "\" is "
        + "provided by multiple source files. Source files: "
        + Arrays.toString(sourceFiles.toArray()));

    this.providedNamespace = providedNamespace;
    this.sourceFiles = sourceFiles;
  }

  /**
   * The namespace that is {@code goog.provided} by multiple source files.
   *
   * @return the {@code goog.provided} namespace
   */
  public String getProvidedNamespace() {
    return this.providedNamespace;
  }

  /**
   * The source files that {@code goog.provide} the namespace.
   *
   * @return the source file
   */
  public List<? extends ProvidesRequiresSourceFile> getSourceFiles() {
    return this.sourceFiles;
  }
}