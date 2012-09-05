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

package org.closureant.soy;

import java.io.File;

import org.apache.tools.ant.BuildException;

/**
 * Data type for nested elements to specify the output Java package and format
 * to use for class names when generating Java classes containing info parsed
 * from closure template files. This avoids the error-prone process of manually
 * typing template and parameter names as strings. There will be one Java
 * class per Soy file.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class JavaParseInfo {

  private File outputDirectory;
  private String outputJavaPackage;
  private String sourceOfClassnames;

  public JavaParseInfo() {
    this.outputDirectory = null;
    this.sourceOfClassnames = null;
    this.outputJavaPackage = null;
  }

  /**
   * Sets the output directory where the Java classes are to be written.
   *
   * @param outputDirectory the output directory
   */
  public void setOutputDirectory(File outputDirectory) {
    if (!outputDirectory.isDirectory()) {
      throw new BuildException("outputDirectory ["
          + outputDirectory.getAbsolutePath() + "] is not a directory");
    }
    this.outputDirectory = outputDirectory;
  }

  /** @return the output directory where Java classes are to be written */
  public File getOutputDirectory() {
    return this.outputDirectory;
  }

  /**
   * Sets the source of the generated class names. Must be one of "filename",
   * "namespace", or "generic".
   * 
   * <p><ul>
   * <li><b>filename</b> - Generates the class AaaBbbSoyInfo for a Soy file
   * named AaaBbb.soy or aaa_bbb.soy.</li>
   * <li><b>namespace</b> - Appends SoyInfo to the last part of a namespace.
   * For example, generates a class called CccDddSoyInfo for the namespace
   * aaa.bbb.cccDdd.</li>
   * <li><b>generic</b> - Generates generic class names such as File1SoyInfo,
   * File2SoyInfo, etc, enumerating these names in the same order as you
   * passed them in.</li>
   * </ul></p>
   *
   * @param sourceOfClassnames the source of the generated class names.
   *     Options: "filename", "namespace", or "generic".
   */
  public void setSourceOfClassnames(String sourceOfClassnames) {
    this.sourceOfClassnames = sourceOfClassnames;
  }

  /**
   * @return the source of the generated class names ("filename", "namespace",
   * or "generic")
   */
  public String getSourceOfClassnames() {
    return this.sourceOfClassnames;
  }

  /**
   * Sets the Java package to use for the generated classes.
   *
   * @param outputJavaPackage the Java package name to use for the
   *     generated classes
   */
  public void setOutputJavaPackage(String outputJavaPackage) {
    this.outputJavaPackage = outputJavaPackage;
  }

  /** @return the Java package name to use for the generated classes */
  public String getOutputJavaPackage() {
    return this.outputJavaPackage;
  }
}