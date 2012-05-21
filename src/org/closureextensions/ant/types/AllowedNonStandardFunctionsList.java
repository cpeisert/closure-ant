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

package org.closureextensions.ant.types;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Ant data type for a list of allowed, non-standard CSS functions to pass to
 * the Closure Stylesheets compiler.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class AllowedNonStandardFunctionsList {

  private List<String> allowedNonStandardFunctions;

  public AllowedNonStandardFunctionsList() {
    this.allowedNonStandardFunctions = Lists.newArrayList();
  }

  /**
   * @param functionsList list of allowed, non-standard CSS functions to pass
   *     to the Closue Stylesheets compiler
   */
  public void setFunctionsList(String functionsList) {
    CharMatcher matcher = CharMatcher.WHITESPACE.or(CharMatcher.anyOf(","));
    Iterable<String> splitFunctions = Splitter.on(matcher).omitEmptyStrings()
        .trimResults().split(functionsList);

    for (String allowedFunction : splitFunctions) {
      this.allowedNonStandardFunctions.add(allowedFunction);
    }
  }

  public List<String> getAllowedNonStandardFunctions() {
    return this.allowedNonStandardFunctions;
  }
}