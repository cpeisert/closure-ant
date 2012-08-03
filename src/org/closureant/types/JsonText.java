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

import org.apache.tools.ant.Project;

/**
 * Data type for nested elements that accept JSON either by assigning to
 * the {@code json} attribute or as free-form body text.
 *
 * <p>Example:</p>
 *
 * <p><pre>{@literal
<soyrenderoptions templateName="test.template.name">
  <data json="{'name': 'Bob'}" />
</soyrenderoptions>

<!-- Pass JSON as free-form body text. -->
<soyrenderoptions templateName="test.template.name">
  <data>
    {'name': 'Bob'}
  </data>
</soyrenderoptions>
 * }</pre></p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class JsonText {

  private String json;
  private boolean replaceProperties;

  public JsonText() {
    this.json = "";
    this.replaceProperties = false;
  }

  /**
   * Sets JSON text.
   *
   * @param json JSON text
   */
  public void setJson(String json) {
    this.json = this.json + json;
  }

  /**
   * Gets the JSON text. If {@code replaceProperties} is {@code true},
   * then Ant properties references embedded in the JSON text will be
   * resolved. Otherwise the JSON text will be returned exactly as entered in
   * the Ant build file.
   *
   * @param project the Ant project
   * @return the JSON text
   */
  public String getJson(Project project) {
    if (this.replaceProperties) {
      return project.replaceProperties(this.json);
    } else {
      return this.json;
    }
  }

  /**
   * Determines if Ant properties embedded in the JSON text should be resolved
   * based on the properties set in the current Ant project.
   *
   * @param replaceProperties {@code true} to replace Ant properties.
   *     Defaults to {@code false}.
   */
  public void setReplaceProperties(boolean replaceProperties) {
    this.replaceProperties = replaceProperties;
  }

  /**
   * @return {@code true} if Ant property references embedded in the JSON
   *     will be resolved against the Ant project
   */
  public boolean replaceProperties() {
    return this.replaceProperties;
  }

  /**
   * Set JSON from free-form body text between XML elements.
   *
   * @param json JSON text
   */
  public void addText(String json) {
    this.json = this.json + json;
  }
}