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

package org.closureant.soy;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

/**
 * Represents an exception where a namespace is declared by more than one
 * Soy source file.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class MultipleDeclarationException extends RuntimeException {
  private static final long serialVersionUID = 0L;

  private final String declaredNamespace;
  private final List<SoySourceFile> sourceFiles;

  /**
   * Constructs a new exception with an appropriate detail message and
   * saves the namespace that is declared by multiple Soy source
   * files.
   *
   * @param declaredNamespace namespace that is declared by multiple source
   *     files
   * @param source1 first source file that declares the namespace
   * @param source2 second source file that declares the namespace
   */
  public MultipleDeclarationException(String declaredNamespace,
      SoySourceFile source1, SoySourceFile source2) {
    this(declaredNamespace, ImmutableList.of(source1, source2));
  }

  /**
   * Constructs a new exception with an appropriate detail message and
   * saves the namespace that is declared by multiple Soy source files.
   *
   * @param declaredNamespace namespace that is declared by multiple source
   *     files
   * @param sourceFiles source files that declare the namespace
   */
  public MultipleDeclarationException(String declaredNamespace,
                                      List<SoySourceFile> sourceFiles) {
    super("namespace \"" + declaredNamespace + "\" is "
        + "declared by multiple Soy source files. Source files: "
        + Arrays.toString(sourceFiles.toArray()));

    this.declaredNamespace = declaredNamespace;
    this.sourceFiles = sourceFiles;
  }

  /**
   * The namespace that is declared by multiple source files.
   *
   * @return the declared namespace
   */
  public String getDeclaredNamespace() {
    return this.declaredNamespace;
  }

  /**
   * The Soy source files that declare the namespace.
   *
   * @return the source file
   */
  public List<SoySourceFile> getSourceFiles() {
    return this.sourceFiles;
  }
}