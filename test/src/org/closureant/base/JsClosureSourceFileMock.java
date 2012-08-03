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

package org.closureant.base;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * A mock implementation of {@link org.closureant.base.JsClosureSourceFile} for testing.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class JsClosureSourceFileMock implements JsClosureSourceFile {

  private String absolutePath;
  private String sourceCode;
  private String name;
  private List<String> provides;
  private String relativePath;
  private List<String> requires;
  private boolean isBaseJs;

  public JsClosureSourceFileMock(String name) {
    this.absolutePath = name;
    this.sourceCode = "";
    this.name = name;
    this.provides = Lists.newArrayList();
    this.relativePath = name;
    this.requires = Lists.newArrayList();
    this.isBaseJs = false;
  }

  public String getAbsolutePath() {
    return absolutePath;
  }

  public void setAbsolutePath(String absolutePath) {
    this.absolutePath = absolutePath;
  }

  public String getCode() {
    return sourceCode;
  }

  public void setCode(String sourceCode) {
    this.sourceCode = sourceCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Collection<String> getProvides() {
    return provides;
  }

  public void setProvides(Collection<String> provides) {
    this.provides = Lists.newArrayList(provides);
  }

  public String getRelativePath() {
    return relativePath;
  }

  public void setRelativePath(String relativePath) {
    this.relativePath = relativePath;
  }

  public boolean isBaseJs() {
    return isBaseJs;
  }

  public void setBaseJs(boolean baseJs) {
    isBaseJs = baseJs;
  }

  public Collection<String> getRequires() {
    return requires;
  }

  public void setRequires(Collection<String> requires) {
    this.requires = Lists.newArrayList(requires);
  }

  @Override
  public String toString() {
    return this.absolutePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    JsClosureSourceFileMock that = (JsClosureSourceFileMock) o;

    if (isBaseJs != that.isBaseJs) {
      return false;
    }
    if (!absolutePath.equals(that.absolutePath)) {
      return false;
    }
    if (!name.equals(that.name)) {
      return false;
    }
    if (!provides.equals(that.provides)) {
      return false;
    }
    if (!relativePath.equals(that.relativePath)) {
      return false;
    }
    if (!requires.equals(that.requires)) {
      return false;
    }
    if (sourceCode != null ? !sourceCode.equals(that.sourceCode) : that
        .sourceCode != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = absolutePath.hashCode();
    result = 31 * result + (sourceCode != null ? sourceCode.hashCode() : 0);
    result = 31 * result + name.hashCode();
    result = 31 * result + provides.hashCode();
    result = 31 * result + relativePath.hashCode();
    result = 31 * result + requires.hashCode();
    result = 31 * result + (isBaseJs ? 1 : 0);
    return result;
  }
}