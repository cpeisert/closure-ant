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
 * Data type for nested elements that have a {@code packages} attribute
 * that accepts a list of active delegate package names separated by
 * whitespace and/or commas.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class SoyActiveDelegatePackageList {

  private List<String> packages;

  public SoyActiveDelegatePackageList() {
    this.packages = Lists.newArrayList();
  }

  /**
   * @param packageNames list of active delegate packages delimited by
   *     whitespace and/or commas
   */
  public void setPackages(String packageNames) {
    CharMatcher matcher = CharMatcher.WHITESPACE.or(CharMatcher.anyOf(","));
    Iterable<String> splitPackageNames = Splitter
        .on(matcher).omitEmptyStrings().trimResults().split(packageNames);

    for (String packageName : splitPackageNames) {
      this.packages.add(packageName.trim());
    }
  }

  public List<String> getPackages() {
    return this.packages;
  }
}
