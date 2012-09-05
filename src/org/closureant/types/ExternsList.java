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

import java.util.List;

/**
 * Data type for nested elements that have an {@code externs} attribute
 * that accepts a list of externs file names separated by whitespace and/or
 * commas.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ExternsList extends StringList {

  public ExternsList() {
    super();
  }

  /**
   * Sets the externs files, where each file may be delimited by whitespace
   * and/or commas.
   *
   * @param externs list of externs files delimited by whitespace and/or
   *     commas
   */
  public void setExterns(String externs) {
    setListOfStrings(externs);
  }

  /**
   * Gets the list of externs files.
   *
   * @return a {@link java.util.List} of externs
   */
  public List<String> getExterns() {
    return getListOfStrings();
  }
}
