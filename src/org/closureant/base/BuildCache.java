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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.closureant.util.FileUtil;

/**
 * A file-based cache to temporarily store settings related to a build
 * process, such as the command line and source files. The build settings are
 * stored relative to an executing Ant task using the task's name and its
 * owning target to uniquely identify it. A directory named ".closure-ant"
 * is created in the current Ant project's base directory to store the settings.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class BuildCache {

  private static final String antClosureToolsMetaDirectory =
      ".closure-ant";
  private static final String fileNamePattern = "task[%s]__target[%s].ser";
  private static final int CACHE_EXPIRATION_DAYS = 2;

  private final File cacheFile;
  private final File baseDirectory;

  public BuildCache(Task antTask) {
    this.baseDirectory = new File(antTask.getProject().getBaseDir(),
        antClosureToolsMetaDirectory);
    baseDirectory.mkdir();
    this.cacheFile = new File(baseDirectory,
        String.format(fileNamePattern, antTask.getTaskName(),
            antTask.getOwningTarget().getName()));
    cleanUpSettingsOlderThanDays(CACHE_EXPIRATION_DAYS);
  }

  /**
   * Get the build settings for this Ant task and its owning target from the
   * file cache.
   *
   * @return the build settings or {@code null} if no build settings found
   *     for this Ant task and its owning target
   */
  public BuildSettings get() {
    if (!this.cacheFile.exists()) {
      return null;
    }

    BuildSettings settings = null;
    ObjectInputStream in = null;
    try {
      FileInputStream fis = new FileInputStream(this.cacheFile);
      in = new ObjectInputStream(fis);
      settings = (BuildSettings) in.readObject();
      in.close();
    } catch (IOException e) {
      throw new BuildException(e);
    } catch (ClassNotFoundException e) {
      throw new BuildException(e);
    } finally {
      try {
        if (in != null) in.close();
      } catch (IOException e) {
        // nothing to see here
      }
    }

    return settings;
  }

  /**
   * Gets the base directory used to cache temporary files.
   *
   * @return the base directory for caching temporary files
   */
  public File getBaseDirectory() {
    return this.baseDirectory;
  }

  /**
   * Save build settings to a file cache.
   *
   * @param settings the build settings to save
   */
  public void put(BuildSettings settings) {
    ObjectOutputStream out = null;
    try {
      FileOutputStream fos = new FileOutputStream(this.cacheFile);
      out = new ObjectOutputStream(fos);
      out.writeObject(settings);
      out.close();
    } catch (IOException e) {
      throw new BuildException(e);
    } finally {
      try {
        if (out != null) out.close();
      } catch (IOException e) {
        // nothing to see here
      }
    }
  }

  /**
   * Creates a temporary file in the Ant Closure Tools base directory
   * designated for the current Ant project. The file will be automatically
   * deleted the next time a BuildCache object is instantiated after {@link
   * #CACHE_EXPIRATION_DAYS}.
   *
   * @param fileName the file name to use for the temporary file
   * @return the temporary file
   */
  public File createTempFile(String fileName) {
    return new File(this.baseDirectory, fileName);
  }

  /**
   * Clear cache settings for the current Ant project older than {@code days}
   * days.
   *
   * @param days days after which cache settings will be deleted
   */
  private void cleanUpSettingsOlderThanDays(int days) {
    FileUtil.deleteFilesOlderThanNumberOfDays(this.baseDirectory, "*", days);
  }
}
