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

import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * Data type for nested elements that have a {@code fileExtensions} attribute
 * that accepts a list of strings separated by whitespace and/or commas.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class FileExtensionList extends StringList {

  public FileExtensionList() {
    super();
  }

  /**
   * Sets the file extensions, where each extension may be delimited by
   * whitespace and/or commas.
   *
   * @param fileExtensions list of file extensions delimited by whitespace
   *     and/or commas
   */
  public void setFileExtensions(String fileExtensions) {
    setListOfStrings(fileExtensions);
  }

  /**
   * Gets the list of file extensions.
   *
   * @return a {@link List} of file extensions
   */
  public Set<String> getFileExtensions() {
    List<String> fileExtensions = getListOfStrings();
    Set<String> fileExtensionsNoDotPrefix = Sets.newHashSet();

    for (String ext : fileExtensions) {
      ext = (ext.startsWith(".")) ? ext.substring(1) : ext;
      fileExtensionsNoDotPrefix.add(ext);
    }
    return fileExtensionsNoDotPrefix;
  }
}
