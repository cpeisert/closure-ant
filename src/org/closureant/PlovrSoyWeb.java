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

import com.google.common.base.Strings;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;

import org.closureant.base.SharedAntProperty;
import org.closureant.util.StringUtil;

/**
 * Ant task for the <a target="_blank" href="http://plovr.com/soyweb.html">
 * plovr SoyWeb</a> web server. The default task name is {@code plovr-soyweb}
 * as defined in "task-definitions.xml".
 *
 * <p>The location of the plovr Jar file is also defined in
 * "closure-ant-config.xml", which should be included in your build file as
 * follows:</p>
 *
 * <p>{@literal <import file="your/path/to/closure-ant-config.xml" />}</p>
 *
 * <p><i>Verify that the paths defined in "closure-ant-config.xml" are
 * correct for your local configuration.</i></p>
 *
 *
 * TODO(cpeisert): Move the Ant-style documentation below into separate doc
 * files.
 *
 *
 * <ul class="blockList">
 * <li class="blockList">
 * <h3>Attributes</h3>
 *
 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0">
 * <col width="20%"/>
 * <col width="60%"/>
 * <col width="20%"/>
 * <thead>
 * <tr><th>Attribute Name</th><th>Description</th><th>Required</th></tr>
 * </thead>
 * <tbody>
 * <tr class="altColor"><td><b>dir</b></td><td>Specifies the directory of files
 *     to serve. No files in parent directories of {@code dir} will be
 *     served by SoyWeb.</td><td><b>Yes</b>.</td></tr>
 * <tr class="rowColor"><td><b>globals</b></td><td>Refers to a JSON file
 *     containing a map of global variable names to values. If supplied,
 *     SoyWeb will supply these values as globals when rendering a template.
 *     See <a target="_blank"
 *     href="http://code.google.com/p/plovr/source/browse/www-globals.js">
 *     www-globals.js</a> for an example of this.</td><td>No.</td></tr>
 * <tr class="altColor"><td><b>noIndexes</b></td><td>Disables listing the
 *     files in a directory, much like the {@code -Indexes} directive in an
 *     Apache config.</td><td>No.</td></tr>
 * <tr class="rowColor"><td><b>plovrJar</b></td><td>The plovr jar file to
 *     execute.</td><td>No, as long as your build file imports
 *     closure-ant-config.xml.</td></tr>
 * <tr class="altColor"><td><b>port</b></td><td>The port on which SoyWeb will
 *     handle requests. The default is 9811 (which is one more than plovr's
 *     default port, 9810).</td><td>No.</td></tr>
 * <tr class="rowColor"><td><b>static</b></td><td>If specified, disables
 *     SoyWeb's default serving behavior, which is to reload a template each
 *     time it is requested. This option is not generally recommended for
 *     use, though admittedly it is used to serve the static content on <a
 *     target="_blank" href="http://plovr.com/">plovr.com</a>.</td><td>No.
 *     </td></tr>
 * <tr class="altColor"><td><b>template</b></td><td>The name of the template
 *     that SoyWeb will use in a {@code .soy} file when it is requested. As
 *     shown in the example, the default value is soyweb. Note that this
 *     template generally does not have any parameters unless it is intended
 *     to be used with {@code unsafe} such that parameter values will be
 *     supplied via the query string.</td><td>No.</td></tr>
 * <tr class="rowColor"><td><b>unsafe</b></td><td>Makes it possible to
 *     specify Soy parameters using query parameters from the URL. This
 *     parameter is named "unsafe" because it opens up the possibility for
 *     XSS attacks. This is not a sensible thing to do if SoyWeb is used in
 *     production, but it is incredibly useful when prototyping internally.
 *     See <a target="_blank"
 *     href="http://plovr.com/soyweb.html#queryparams">Setting template
 *     parameters with URL query parameters</a> for details.</td><td>No.</td>
 *     </tr>
 * </tbody>
 * </table>
 * </li>
 * </ul>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class PlovrSoyWeb extends Task {

  // Attributes

  private String directory;
  private String globals;
  private boolean noIndexes;
  private String plovrJar;
  private int port;
  private boolean serveStatic;
  private String template;
  private boolean unsafe;


  /**
   * Constructs a new Ant task for plovr SoyWeb.
   */
  public PlovrSoyWeb() {

    // Attributes

    this.directory = null;
    this.globals = null;
    this.noIndexes = false;    
    this.plovrJar = null;
    this.port = -1;
    this.serveStatic = false;
    this.template = null;
    this.unsafe = true;
  }

  // TODO(cpeisert): change param docs to method docs
  // Attribute setters

  /**
   * @param dir the only required option: it specifies the directory of files
   *     to serve. No files in parent directories of {@code dir} will be
   *     served by SoyWeb.
   */
  public void setDir(String dir) {
    this.directory = dir;
  }

  /**
   * @param globals refers to a JSON file containing a map of global variable
   *     names to values. If supplied, SoyWeb will supply these values as
   *     globals when rendering a template. See <a target="_blank"
   *     href="http://code.google.com/p/plovr/source/browse/www-globals.js">
   *     www-globals.js</a> for an example of this.
   */
  public void setGlobals(String globals) {
    this.globals = globals;
  }

  /**
   * @param noIndexes disables listing the files in a directory, much like
   *     the {@code -Indexes} directive in an Apache config.
   */
  public void setNoIndexes(boolean noIndexes) {
    this.noIndexes = noIndexes;
  }

  /**
   * @param plovrJar the plovr jar file to execute
   */
  public void setPlovrJar(String plovrJar) {
    this.plovrJar = plovrJar;
  }
  
  /**
   * @param port is the port on which SoyWeb will handle requests. The default
   *     is 9811 (which is one more than plovr's default port, 9810).
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * @param serveStatic if specified, disables SoyWeb's default serving
   *     behavior, which is to reload a template each time it is requested.
   *     This option is not generally recommended for use, though admittedly
   *     it is used to serve the static content on <a target="_blank"
   *     href="http://plovr.com/">plovr.com</a>.
   */
  public void setStatic(boolean serveStatic) {
    this.serveStatic = serveStatic;
  }

  /**
   * @param template is the name of the template that SoyWeb will use in a
   *     {@code .soy} file when it is requested. As shown in the example,
   *     the default value is soyweb. Note that this template generally does
   *     not have any parameters unless it is intended to be used with
   *     {@code unsafe} such that parameter values will be supplied via the
   *     query string.
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * @param unsafe makes it possible to specify Soy parameters using query
   *     parameters from the URL. This parameter is named "unsafe" because it
   *     opens up the possibility for XSS attacks. This is not a sensible
   *     thing to do if SoyWeb is used in production, but it is incredibly
   *     useful when prototyping internally. See <a target="_blank"
   *     href="http://plovr.com/soyweb.html#queryparams">Setting template 
   *     parameters with URL query parameters</a> for details.
   */
  public void setUnsafe(boolean unsafe) {
    this.unsafe = unsafe;
  }


  /**
   * Execute the plovr SoyWeb task.
   *
   * @throws org.apache.tools.ant.BuildException on error
   */
  @Override
  public void execute() {

    // Verify task preconditions

    if (this.plovrJar == null) {
      String plovrPath = SharedAntProperty.PLOVR_JAR.getValue(getProject());
      if (plovrPath != null) {
        this.plovrJar = plovrPath;
      } else {
        throw new BuildException("Required attribute \"plovrJar\" is not "
            + "set. Verify that your build file imports "
            + "\"closure-ant-config.xml\" and that the property paths are "
            + "correct for your machine.");
      }
    }
    
    if (Strings.isNullOrEmpty(this.directory)) {
      throw new BuildException("required attribute \"dir\" is not set");
    }    

    // Prepare Java Ant task to run the plovr jar file.
    
    Java javaTask = (Java) getProject().createTask("java");
    javaTask.setJar(new File(this.plovrJar));
    javaTask.setFailonerror(true);
    javaTask.setFork(true);
    javaTask.setLogError(true);
    javaTask.setTaskName(getTaskName());

    // Set command line arguments.

    javaTask.createArg().setValue("soyweb");
    javaTask.createArg().setLine("--dir " 
        + StringUtil.quoteString(this.directory));
    if (this.unsafe) {
      javaTask.createArg().setValue("--unsafe");
    }
        
    String soyWebAddress = "localhost";
    int soyWebPort = 9811;

    if (this.port >= 0) {
      javaTask.createArg().setLine("--port " + this.port);
      soyWebPort = this.port;
    }    
    if (this.template != null) {
      javaTask.createArg().setLine("--template " 
          + StringUtil.quoteString(this.template));
    }    
    if (this.globals != null) {
      javaTask.createArg().setLine("--globals "
          + StringUtil.quoteString(this.globals));  
    }
    if (this.serveStatic) {
      javaTask.createArg().setValue("--static");
    }
    if (this.noIndexes) {
      javaTask.createArg().setValue("--noindexes");
    }
   
    // Spawn the plovr SoyWeb server java process so that the Ant task exits.
    javaTask.setSpawn(true);
    javaTask.executeJava();
    log("plovr SoyWeb server started. Visit http://" + soyWebAddress
        + ":" + soyWebPort, Project.MSG_INFO);   
  }
}