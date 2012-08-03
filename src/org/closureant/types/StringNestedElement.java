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

/**
 * Data type for simple nested elements that have a generic string value
 * attribute.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class StringNestedElement {

  private String value;

  public StringNestedElement() {
    this.value = "";
  }

  /**
   * @param value the attribute value
   */
  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}