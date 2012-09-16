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

package org.closureant;

import com.google.common.io.Resources;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Ant task to generate HTML documentation for the Closure Ant project.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ClosureAntDoc extends Task {

  // Attributes

  private File antMetaDocFile;
  private File html5BoilerplateSoyTemplate;
  private File outputDirectory;

  // Nested elements



  /**
   * Constructs a new Ant task for Closure Builder.
   */
  public ClosureAntDoc() {
    // Attributes
    this.antMetaDocFile = null;
    this.html5BoilerplateSoyTemplate = Resources.getResource(getClass(),
        "html5-boilerplate.soy");
    this.outputDirectory = new File(".");

    // Nested elements

  }


  // Attribute setters

  /**
   * Sets the Ant Meta Doc JSON file. See <a target="_blank"
   * href="https://github.com/cpeisert/ant-meta-doc">Ant Meta Doc</a>.
   *
   * @param file The Ant Meta Doc JSON file.
   */
  public void setAntMetaDocFile(File file) {
    this.antMetaDocFile = file;
  }

  public void setOutputDirectory(File directory) {
    this.outputDirectory = directory;
  }


  // Nested elements



  /**
   * Executes the Closure Ant Doc task.
   *
   * @throws org.apache.tools.ant.BuildException on error.
   */
  @Override
  public void execute() {
    try {// execute() cannot throw checked IOException due to parent definition
      if (this.antMetaDocFile == null) {
        throw new BuildException("required attribute antMetaDocFile not set");
      }
      if (!this.outputDirectory.isDirectory()) {
        throw new BuildException("Attribute outputDirectory must be a "
            + "directory. Found value: " + outputDirectory.getAbsolutePath());
      }



    } catch (IOException e) {
      throw new BuildException(e);
    }
  }
}
