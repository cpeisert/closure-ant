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
 * Simple data type for a name-value pair.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class NameValuePair {

  private String name;
  private String value;

  public NameValuePair() {
    this.name = null;
    this.value = null;
  }
  
  public NameValuePair(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * @param value the value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    NameValuePair that = (NameValuePair) o;

    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (value != null ? !value.equals(that.value) : that.value != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "NameValuePair{" +
        "name='" + name + '\'' +
        ", value='" + value + '\'' +
        '}';
  }
}