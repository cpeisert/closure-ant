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
 * Data type for nested elements that have a {@code locales} attribute
 * that accepts a list of strings separated by whitespace and/or commas.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class LocaleList extends StringList {

  public LocaleList() {
    super();
  }

  /**
   * Sets the locales, where each locale may be delimited by whitespace
   * and/or commas.
   *
   * @param locales list of locales delimited by whitespace and/or commas
   */
  public void setLocales(String locales) {
    setListOfStrings(locales);
  }

  /**
   * Gets the list of locales.
   *
   * @return a {@link List} of file extensions
   */
  public List<String> getLocales() {
    return getListOfStrings();
  }
}
