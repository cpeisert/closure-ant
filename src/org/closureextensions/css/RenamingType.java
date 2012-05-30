/*
 * Copyright 2011 Google Inc.
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

package org.closureextensions.css;

import com.google.common.css.IdentitySubstitutionMap;
import com.google.common.css.MinimalSubstitutionMap;
import com.google.common.css.SimpleSubstitutionMap;
import com.google.common.css.SplittingSubstitutionMap;
import com.google.common.css.SubstitutionMap;
import com.google.common.css.SubstitutionMapProvider;

/**
 * {@link RenamingType} is an enumeration of the possible values for the
 * {@code --rename} option in {@link
 * com.google.common.css.compiler.commandline.ClosureCommandLineCompiler}.
 * Each corresponds to an implementation of {@link
 * com.google.common.css.SubstitutionMapProvider} that creates a {@link
 * com.google.common.css.SubstitutionMap} to reflect the type of renaming.
 *
 * <p><b>Note</b>: This enum is a repackaged copy of {@link
 * com.google.common.css.compiler.commandline.RenamingType}, which is package
 * private.</p>
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public enum RenamingType {
  /** No renaming is done. */
  NONE(new SubstitutionMapProvider() {
    @Override
    public SubstitutionMap get() {
      return new IdentitySubstitutionMap();
    }
  }),

  /** A trailing underscore is added to each part of a CSS class. */
  DEBUG(new SubstitutionMapProvider() {
    @Override
    public SubstitutionMap get() {
      // This wraps the SimpleSubstitutionMap in a SplittingSubstitutionMap so
      // that can be used with goog.getCssName().
      return new SplittingSubstitutionMap(new SimpleSubstitutionMap());
    }
  }),


  /**
   * Each chunk of a CSS class as delimited by '-' is renamed using the
   * shortest available name.
   */
  CLOSURE(new SubstitutionMapProvider() {
    @Override
    public SubstitutionMap get() {
      return new SplittingSubstitutionMap(new MinimalSubstitutionMap());
    }
  }),
  ;

  private final SubstitutionMapProvider provider;

  private RenamingType(SubstitutionMapProvider provider) {
    this.provider = provider;
  }

  public SubstitutionMapProvider getCssSubstitutionMapProvider() {
    return provider;
  }
}
