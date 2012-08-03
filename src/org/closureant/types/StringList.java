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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Abstract base class for Ant data types that accept a list of strings
 * separated by whitespace and/or commas.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public abstract class StringList {

  private List<String> listOfStrings;

  protected StringList() {
    this.listOfStrings = Lists.newArrayList();
  }

  /**
   * @param listOfStrings list of strings delimited by whitespace and/or commas
   */
  protected void setListOfStrings(String listOfStrings) {
    CharMatcher matcher = CharMatcher.WHITESPACE.or(CharMatcher.anyOf(","));
    Iterable<String> splitStrings = Splitter.on(matcher).omitEmptyStrings()
        .trimResults().split(listOfStrings);

    for (String string : splitStrings) {
      this.listOfStrings.add(string);
    }
  }

  /** @return the {@link List} of strings */
  protected List<String> getListOfStrings() {
    return this.listOfStrings;
  }
}
