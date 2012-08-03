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

import org.closureant.base.ProvidesRequiresSourceFile;

/**
 * Represents a missing provide exception, which occurs when a source file
 * {@code goog.requires} a namespace that is never {@code goog.provided}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class MissingProvideException extends RuntimeException {
  private static final long serialVersionUID = 0L;

  private final String requiredNamespace;
  private final ProvidesRequiresSourceFile sourceFile;

  /**
   * Constructs a new exception with an appropriate detail message and
   * saves the namespaces that are {@code goog.required} by the source file
   * {@code sourceFile} but never {@code goog.provided}.
   *
   * @param requiredNamespace namespace that is {@code goog.required} but
   *     never {@code goog.provided}
   * @param sourceFile source file that {@code goog.required} the namespace
   */
  public MissingProvideException(
      String requiredNamespace, ProvidesRequiresSourceFile sourceFile) {

    super("goog.required namespace \"" + requiredNamespace + "\" never "
        + "provided. Source file: \"" + sourceFile.getAbsolutePath() + "\".");

    this.requiredNamespace = requiredNamespace;
    this.sourceFile = sourceFile;
  }

  /**
   * Constructs a new exception indicating a required namespace is never
   * {@code goog.provided}.
   *
   * @param requiredNamespace the required namespace
   * @param message the detail message
   */
  MissingProvideException(String requiredNamespace, String message) {
    super(message);

    this.requiredNamespace = requiredNamespace;
    this.sourceFile = null;
  }

  /**
   * The namespace that is {@code goog.required} but never {@code
   * goog.provided}.
   *
   * @return the {@code goog.required} namespace
   */
  public String getRequiredNamespace() {
    return this.requiredNamespace;
  }

  /**
   * The source file that {@code goog.required} the namespace.
   *
   * @return the source file
   */
  public ProvidesRequiresSourceFile getSourceFile() {
    return this.sourceFile;
  }
}