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

/**
 * Data type for attribute {@code idgenerator} of the nested
 * {@literal <compiler>} element. {@code idgenerator} corresponds to the
 * plovr JSON config file option {@code id-generators}.
 *
 * @author cpeisert@gmail.com (Christopher Peisert)
 */
public final class IdGenerator {

  private String idGenerator;

  public IdGenerator() {
    this.idGenerator = "";
  }

  /**
   * @param idGenerator the attribute idGenerator
   */
  public void setIdGenerator(String idGenerator) {
    this.idGenerator = idGenerator;
  }

  public String getIdGenerator() {
    return this.idGenerator;
  }
}