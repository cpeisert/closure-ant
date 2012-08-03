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

import org.apache.tools.ant.BuildException;
import org.closureant.ant.PlovrTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Tests for {@link org.closureant.ant.PlovrTask}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class PlovrTaskTest {

  private PlovrTask plovrTask;

  @Before public void setUp() {
    this.plovrTask = new PlovrTask();
  }

  @Test public void setPlovrModeValidOptions() {
    this.plovrTask.setPlovrMode("build");
    this.plovrTask.setPlovrMode("Config");
    this.plovrTask.setPlovrMode("JSDOC");
    this.plovrTask.setPlovrMode("serve");
  }
  
  @Test(expected = BuildException.class)
  public void setOutputModeSoyweb() {
    this.plovrTask.setPlovrMode("soyweb");
  }

  @Test(expected = BuildException.class)
  public void setOutputModeInvalidOption() {
    this.plovrTask.setPlovrMode("invalid");
  }
  
  @Test public void createCompiler() {
    this.plovrTask.createCompiler();
  }
  
  @Test(expected = BuildException.class)
  public void createTwoCompilers() {
    this.plovrTask.createCompiler();
    this.plovrTask.createCompiler();
  }

  @Test(expected = BuildException.class)
  public void executeInBuildModeWithNullPlovrJar() {
    this.plovrTask.setProject(MockProject.getProject());
    this.plovrTask.setPlovrMode("build");
    this.plovrTask.setPlovrJar(null);    
    this.plovrTask.execute();
  }
}