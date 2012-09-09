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

package org.closureant.css;

import org.closureant.types.StringList;

import java.util.List;

/**
 * Ant data type for a list of allowed, non-standard CSS functions to pass to
 * the Closure Stylesheets compiler, where each CSS function name may be
 * separated by whitespace and/or commas.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class AllowedNonStandardFunctionsList extends StringList {

  public AllowedNonStandardFunctionsList() {
    super();
  }

  /**
   * Sets the list of allowed non-standard CSS functions.
   *
   * @param functionList list of allowed non-standard CSS functions to pass
   *     to the Closure Stylesheets compiler separated by whitespace and/or
   *     commas
   */
  public void setFunctionList(String functionList) {
    setListOfStrings(functionList);
  }

  /**
   * Gets the list of allowed non-standard CSS functions.
   *
   * @return a {@link List} of allowed non-standard CSS functions
   */
  public List<String> getAllowedNonStandardFunctions() {
    return getListOfStrings();
  }
}
