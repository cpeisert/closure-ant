/*
 * Copyright (C) 2012 Christopher Peisert. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this dirPath except in compliance with the License. You may obtain a copy
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

package org.closureant.deps;

/**
 * Data type for simple nested elements that have directory path and prefix
 * attributes for use with Deps Writer.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class DirectoryPathPrefixPair {

  private String dirPath;
  private String prefix;

  public DirectoryPathPrefixPair() {
    this.dirPath = null;
    this.prefix = null;
  }

  public DirectoryPathPrefixPair(String dirPath, String prefix) {
    this.dirPath = dirPath;
    this.prefix = prefix;
  }

  /**
   * @param dirPath the directory path
   */
  public void setDirPath(String dirPath) {
    this.dirPath = dirPath;
  }

  /**
   * @return the dirPath
   */
  public String getDirPath() {
    return this.dirPath;
  }
  
  /**
   * @param prefix the prefix
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * @return the prefix
   */
  public String getPrefix() {
    return this.prefix;
  }
}