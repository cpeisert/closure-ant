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

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;

/**
 * Ant build listener based on implementation in
 * {@code org.apache.tools.ant.BuildFileTest}.
 */
class AntTestListener implements BuildListener {
  private int logLevel;
  private StringBuffer logBuffer;
  private StringBuffer fullLogBuffer;
  
  /**
   * Constructs a test listener which will ignore log events
   * above the given level.
   */
  public AntTestListener(int logLevel) {
    this.logLevel = logLevel;
    this.logBuffer = new StringBuffer(256);
    this.fullLogBuffer = new StringBuffer(1024);
  }

  /**
   * @return the log messages for info, warning, and error if they are greater
   * than {@link #logLevel}
   */
  public String getLog() {
    return this.logBuffer.toString();
  }

  /**
   * @return the log messages for all levels greater than {@link #logLevel}
   */
  public String getFullLog() {
    return this.fullLogBuffer.toString();
  }

  /**
   * clear contents of the logs
   */
  public void clearLogs() {
    this.logBuffer = new StringBuffer(256);
    this.fullLogBuffer = new StringBuffer(1024);
  }

  /**
   * Fired before any targets are started.
   */
  public void buildStarted(BuildEvent event) {
  }

  /**
   * Fired after the last target has finished. This event
   * will still be thrown if an error occurred during the build.
   *
   * @see org.apache.tools.ant.BuildEvent#getException()
   */
  public void buildFinished(BuildEvent event) {
  }

  /**
   * Fired when a target is started.
   *
   * @see org.apache.tools.ant.BuildEvent#getTarget()
   *
   */
  public void targetStarted(BuildEvent event) {
    //System.out.println("targetStarted " + event.getTarget().getName());
  }

  /**
   * Fired when a target has finished. This event will
   * still be thrown if an error occurred during the build.
   *
   * @see org.apache.tools.ant.BuildEvent#getException()
   */
  public void targetFinished(BuildEvent event) {
    //System.out.println("targetFinished " + event.getTarget().getName());
  }

  /**
   * Fired when a task is started.
   *
   * @see org.apache.tools.ant.BuildEvent#getTask()
   */
  public void taskStarted(BuildEvent event) {
    //System.out.println("taskStarted " + event.getTask().getTaskName());
  }

  /**
   * Fired when a task has finished. This event will still
   * be throw if an error occurred during the build.
   *
   * @see org.apache.tools.ant.BuildEvent#getException()
   */
  public void taskFinished(BuildEvent event) {
    //System.out.println("taskFinished " + event.getTask().getTaskName());
  }

  /**
   * Fired whenever a message is logged.
   *
   * @see org.apache.tools.ant.BuildEvent#getMessage()
   * @see org.apache.tools.ant.BuildEvent#getPriority()
   */
  public void messageLogged(BuildEvent event) {
    if (event.getPriority() > this.logLevel) {
      // ignore event
      return;
    }

    if (event.getPriority() == Project.MSG_INFO ||
        event.getPriority() == Project.MSG_WARN ||
        event.getPriority() == Project.MSG_ERR) {
      this.logBuffer.append(event.getMessage()
          + System.getProperty("line.separator"));
    }
    this.fullLogBuffer.append(event.getMessage()
        + System.getProperty("line.separator"));
  }
}
