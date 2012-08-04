/*
 * Copyright 2008 Google Inc.
 * Copyright (C) 2012 Christopher Peisert. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.closureant.types;

import com.google.template.soy.data.SoyMapData;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.closureant.soy.ExtraSoyUtils;
import org.closureant.types.JsonText;

/**
 * Ant type for setting options related to rendering a Soy template,
 * such as the template name, parameter data, and injected data.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class SoyTemplateRenderOptions {

  // Attributes
  private String outputPathFormat;
  private String templateName;

  // Nested elements
  private JsonText data;
  private JsonText ijData;


  /**
   * Constructs a new Ant type to set options related to rendering Soy
   * templates.
   */
  public SoyTemplateRenderOptions() {
    // Attributes
    this.outputPathFormat = null;
    this.templateName = null;

    // Nested elements
    this.data = null;
    this.ijData = null;
  }

  // Attributes

  /**
   * A format string that specifies how to build the path to each output file
   * containing rendered content. If not generating localized content, then
   * there will be one output file (UTF-8) for the specified Soy template. If
   * generating localized JS, then there will be one output JS file for each
   * locale. The format string can include literal characters as well as the
   * placeholders {@code {TEMPLATE_NAME}}, {@code {FULL_TEMPLATE_NAME}},
   * {@code {LOCALE}}, {@code {LOCALE_LOWER_CASE}}.
   *
   * <p><b>Note:</b> {@code {LOCALE_LOWER_CASE}} turns dash into
   * underscore, e.g. {@code pt-BR} becomes {@code pt_br}.</p>
   *
   * @param outputPathFormat format string specifying how to build the path
   *     to each output file
   */
  public void setOutputPathFormat(String outputPathFormat) {
    this.outputPathFormat = outputPathFormat;
  }

  /**
   * Returns the format for generated output files. See {@link
   * #setOutputPathFormat(String)}.
   */
  public String getOutputPathFormat() {
    return this.outputPathFormat;
  }

  /**
   * The full name of the Soy template to render.
   *
   * @param templateName the full template name
   */
  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  /**
   * Get the full template name.
   *
   * @return the full template name
   */
  public String getFullTemplateName() {
    return this.templateName;
  }

  /**
   * Get the short template name, that is, the name following the last period.
   *
   * @return the short template name
   */
  public String getShortTemplateName() {
    int index = this.templateName.lastIndexOf(".");
    return this.templateName.substring(index + 1);
  }


  // Nested elements

  /**
   * Set the Soy template parameters from a JSON object.
   *
   * @param data JSON text to be parsed into Soy template parameters
   */
  public void addData(JsonText data) {
    if (this.data == null) {
      this.data = data;
    } else {
      throw new BuildException("nested element <data> may only be "
          + "used once per enclosing element");
    }
  }

  /**
   * Get the template data as a {@link SoyMapData} object.
   *
   * @param project the Ant project
   * @return the template data as a {@link SoyMapData} object or {@code null}
   *     if not set
   */
  public SoyMapData getTemplateData(Project project) {
    if (this.data == null) {
      return null;
    }
    return ExtraSoyUtils.parseJsonToSoyMapData(this.data.getJson(project));
  }

  /**
   * Set the Soy template injected data from a JSON object.
   *
   * @param ijData JSON text to be parsed into Soy template injected data
   */
  public void addConfiguredIjData(JsonText ijData) {
    if (this.ijData == null) {
      this.ijData = ijData;
    } else {
      throw new BuildException("nested element <ijdata> may only be "
          + "used once per enclosing element");
    }
  }

  /**
   * Get the template injected data as a {@link SoyMapData} object.
   *
   * @param project the Ant project
   * @return the template injected data as a {@link SoyMapData} object or
   *     {@code null} if not set
   */
  public SoyMapData getTemplateInjectedData(Project project) {
    if (this.ijData == null) {
      return null;
    }
    return ExtraSoyUtils.parseJsonToSoyMapData(this.ijData.getJson(project));
  }
}