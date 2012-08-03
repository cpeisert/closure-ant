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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Object to store build settings related to a build process, such as the
 * command line and source files.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class BuildSettings implements Serializable {

  private static final long serialVersionUID = 42L;

  private boolean buildFailed;
  private long buildTime;
  private String commandLineOrConfig;
  private final List<String> sources;

  /**
   * Constructs an empty BuildSettings instance.
   */
  public BuildSettings() {
    this.buildFailed = false;
    this.buildTime = System.currentTimeMillis();
    this.commandLineOrConfig = "";
    this.sources = Lists.newArrayList();
  }

  /**
   * Constructs a BuildSettings instance.
   *
   * @throws NullPointerException if {@code commandLineOrConfig} or
   *     {@code sources} is {@code null}
   */
  public BuildSettings(String commandLineOrConfig, Collection<String> sources) {
    Preconditions.checkNotNull(commandLineOrConfig, "commandLineOrConfig was null");
    Preconditions.checkNotNull(sources, "sources was null");
    this.buildFailed = false;
    this.buildTime = System.currentTimeMillis();
    this.commandLineOrConfig = commandLineOrConfig;
    this.sources = Lists.newArrayList(sources);
  }

  /**
   * Whether the build failed.
   *
   * @return {@code true} if the build failed. Defaults to {@code false}.
   */
  public boolean isBuildFailed() {
    return this.buildFailed;
  }

  /**
   * Sets whether the build failed.
   *
   * @param buildFailed {@code true} indicates that the build failed
   */
  public void setBuildFailed(boolean buildFailed) {
    this.buildFailed = buildFailed;
  }

  /**
   * Gets the build time as the number of milliseconds since the Unix epoch.
   *
   * @return the build time in milliseconds
   */
  public long getBuildTime() {
    return this.buildTime;
  }

  /**
   * Sets the build time in milliseconds since the Unix epoch.
   *
   * @param buildTime the build time in milliseconds since the Unix epoch
   */
  public void setBuildTime(long buildTime) {
    this.buildTime = buildTime;
  }

  /**
   * Get the command line string.
   *
   * @return the command line
   */
  public String getCommandLineOrConfig() {
    return this.commandLineOrConfig;
  }

  /**
   * Set the command line string.
   *
   * @param commandLineOrConfig the command line or config file contents
   * @throws NullPointerException if {@code commandLineOrConfig} is {@code null}
   */
  public void setCommandLineOrConfig(String commandLineOrConfig) {
    Preconditions.checkNotNull(sources, "commandLineOrConfig was null");
    this.commandLineOrConfig = commandLineOrConfig;
  }

  /**
   * Gets an immutable list of the build sources.
   *
   * @return a list of the build sources
   */
  public ImmutableList<String> getSources() {
    return ImmutableList.copyOf(this.sources);
  }

  /**
   * Adds a source path to the build sources.
   *
   * @param source the source file path
   * @throws NullPointerException if {@code source} is {@code null}
   */
  public void addSource(String source) {
    Preconditions.checkNotNull(source, "source was null");
    this.sources.add(source);
  }

  /**
   * Add all source files in a {@link Collection}.
   *
   * @param sources the source files
   * @throws NullPointerException if {@code sources} is {@code null}
   */
  public void addAllSources(Collection<String> sources) {
    Preconditions.checkNotNull(sources, "sources was null");
    this.sources.addAll(sources);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BuildSettings that = (BuildSettings) o;

    if (buildFailed != that.buildFailed) {
      return false;
    }
    if (buildTime != that.buildTime) {
      return false;
    }
    if (!commandLineOrConfig.equals(that.commandLineOrConfig)) {
      return false;
    }
    if (!sources.equals(that.sources)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = (buildFailed ? 1 : 0);
    result = 31 * result + (int) (buildTime ^ (buildTime >>> 32));
    result = 31 * result + commandLineOrConfig.hashCode();
    result = 31 * result + sources.hashCode();
    return result;
  }
}
