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
 * Data type for nested elements that have a {@code namespaces} attribute
 * that accepts a list of strings separated by whitespace and/or commas.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class NamespaceList extends StringList {

  public NamespaceList() {
    super();
  }

  /**
   * Sets the namespaces, where each namespace may be delimited by whitespace
   * and/or commas.
   *
   * @param namespaces list of namespaces delimited by whitespace and/or commas
   */
  public void setNamespaces(String namespaces) {
    setListOfStrings(namespaces);
  }

  /**
   * Gets the list of namespaces.
   *
   * @return a {@link java.util.List} of namespaces
   */
  public List<String> getNamespaces() {
    return getListOfStrings();
  }
}
