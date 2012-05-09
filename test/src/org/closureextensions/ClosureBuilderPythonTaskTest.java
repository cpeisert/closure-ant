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

package org.closureextensions;

import org.apache.tools.ant.BuildException;

import org.closureextensions.ant.ClosureBuilderPythonTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Tests for {@link org.closureextensions.ant.ClosureBuilderPythonTask}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class ClosureBuilderPythonTaskTest {
  
  private ClosureBuilderPythonTask closureBuilder;
  
  @Before public void setUp() {
    this.closureBuilder = new ClosureBuilderPythonTask();
  }

  @Test public void setOutputModeValidOptions() {
    this.closureBuilder.setOutputMode("RAW");
    this.closureBuilder.setOutputMode("compiled");
  }
  
  @Test(expected = BuildException.class)
  public void setOutputModeInvalidOption() {
    this.closureBuilder.setOutputMode("invalid");
  }
  
  @Test public void createCompiler() {
    this.closureBuilder.createCompiler();
  }
  
  @Test(expected = BuildException.class)
  public void createTwoCompilers() {
    this.closureBuilder.createCompiler();
    this.closureBuilder.createCompiler();
  }
}