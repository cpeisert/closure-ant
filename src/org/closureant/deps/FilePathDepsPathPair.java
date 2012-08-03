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

package org.closureant.deps;

/**
 * Data type for simple nested elements that have file path and deps path 
 * attributes for use with Deps Writer.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class FilePathDepsPathPair {

  private String filePath;
  private String depsPath;

  public FilePathDepsPathPair() {
    this.filePath = null;
    this.depsPath = null;
  }

  public FilePathDepsPathPair(String filePath, String depsPath) {
    this.filePath = filePath;
    this.depsPath = depsPath;
  }

  /**
   * @param filePath the file path
   */
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  /**
   * @return the file path
   */
  public String getFilePath() {
    return this.filePath;
  }
  
  /**
   * @param depsPath the depsPath
   */
  public void setDepsPath(String depsPath) {
    this.depsPath = depsPath;
  }

  /**
   * @return the depsPath
   */
  public String getDepsPath() {
    return this.depsPath;
  }
}