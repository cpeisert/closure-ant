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

package org.closureant.types;

import org.closureant.types.StringList;

import java.util.List;

/**
 * Ant data type for a list of Closure Stylesheets conditionals that are
 * defined as true when the GSS/CSS stylesheets are compiled.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class DefinedTrueConditionalsList extends StringList {

  public DefinedTrueConditionalsList() {
    super();
  }

  /**
   * Sets the conditionals to be set to {@code true}, where each conditional is
   * delimited by whitespace and/or commas.
   *
   * @param trueConditionals list of conditionals to be set to {@code true}
   */
  public void setTrueConditionalsList(String trueConditionals) {
    setListOfStrings(trueConditionals);
  }

  /**
   * Gets the list of {@code true} conditionals.
   *
   * @return a {@link List} of {@code true} conditions
   */
  public List<String> getTrueConditionals() {
    return getListOfStrings();
  }
}
