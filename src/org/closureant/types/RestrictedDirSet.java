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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;

import org.closureant.util.FileUtil;

/**
 * Provides a restricted directory-set data type similar to Ant's {@link
 * DirSet}. Unlike {@link DirSet}, a {@link RestrictedDirSet} only contains
 * common base directories for each branch of the matched directory tree.
 * See {@link RestrictedDirSet#getMatchedDirectories()} and {@link
 * org.closureant.base.CommonBaseDirectoryTree}</p>
 *
 * <p><h3>Examples</h3></p>
 *
 * <p><b>Directory structure</b></p>
 * <pre>
 * + /home
 * |--+ bob
 * |  |--+ project
 * |     |--+ lib
 * |     |  |--- src
 * |     |--+ test
 * |        |--- src
 * |--+ joe
 *    |--+ project
 *       |--+ src
 *          |--+ subproject
 *             |--- src
 * </pre>
 *
 * <p><b>No includes or excludes pattern (root directory only)</b></p>
 * <pre>{@literal
<restricteddirset dir="/home" id="matchedDirs" />
<pathconvert property="dirList" refid="matchedDirs" pathsep="${line.separator}" />
<echo message="${dirList}" />
 * }</pre>
 * <b>Echoes:</b>
 * <pre>{@code
/home
 * }</pre></p>
 *
 * <p><b>Set includes and excludes patterns (recursive scan)</b></p>
 * <pre>{@literal
<restricteddirset dir="/home" id="matchedDirs2"}
    includes="*&#042;/src/**" excludes="*&#042;/test/**" />
{@literal
<pathconvert property="dirList2" refid="matchedDirs2" pathsep="${line.separator}" />
<echo message="${dirList2}" />
 * }</pre>
 * <b>Echoes:</b>
 * <pre>{@code
/home/bob/project/lib/src
/home/joe/project/src
 * }</pre>
 * <b>Note:</b> Although directory {@code /home/joe/project/src/subproject/src}
 * matches the {@code includes} pattern, it is not included since it is a
 * subdirectory of the matching directory {@code /home/joe/project/src}.
 * From the perspective of a {@link
 * org.closureant.base.CommonBaseDirectoryTree} both {@code
 * /home/joe/project/src} and {@code /home/joe/project/src/subproject/src}
 * share the common base directory {@code /home/joe/project/src}.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class RestrictedDirSet extends DataType
    implements ResourceCollection {

  private FileSet fileset;
  private boolean includesExcludesSet;

  /**
   * Constructs a {@link RestrictedDirSet} object with the {@code
   * matchRecursive} attribute set to {@code false}.
   */
  public RestrictedDirSet() {
    super();
    this.fileset = new FileSet();
    this.includesExcludesSet = false;
  }

  /**
   * The root directory to scan.
   *
   * @param directory the root directory to scan
   * @throws BuildException if the canonical file for the specified directory
   *     cannot be obtained
   */
  public void setDir(File directory) {
    try {
      this.fileset.setDir(directory.getCanonicalFile());
    } catch (IOException e) {
      throw new BuildException(e);
    }
  }

  /**
   * Gets the root directory.
   *
   * @return the root directory
   */
  public File getDir() {
    return this.fileset.getDir();
  }

  /**
   * Sets whether default exclusions should be used or not.
   *
   * @param useDefaultExcludes "true"|"on"|"yes" when default exclusions
   *     should be used, "false"|"off"|"no" when they should not be used
   */
  public void setDefaultexcludes(boolean useDefaultExcludes) {
    this.includesExcludesSet = true;
    fileset.setDefaultexcludes(useDefaultExcludes);
  }

  /**
   * Sets the set of exclude patterns. Patterns may be separated by a comma
   * or a space.
   *
   * @param excludes the string containing the exclude patterns
   */
  public void setExcludes(String excludes) {
    this.includesExcludesSet = true;
    fileset.setExcludes(excludes);
  }

  /**
   * Sets the name of the file containing the includes patterns.
   *
   * @param excludesfile a string containing the filename to fetch
   *     the include patterns from
   */
  public void setExcludesfile(File excludesfile) {
    this.includesExcludesSet = true;
    fileset.setExcludesfile(excludesfile);
  }

  /**
   * Sets the set of include patterns. Patterns may be separated by a comma
   * or a space.
   *
   * @param includes the string containing the include patterns
   */
  public void setIncludes(String includes) {
    this.includesExcludesSet = true;
    fileset.setIncludes(includes);
  }

  /**
   * Sets the name of the file containing the includes patterns.
   *
   * @param includesfile a string containing the filename to fetch
   *     the include patterns from
   */
  public void setIncludesfile(File includesfile) {
    this.includesExcludesSet = true;
    fileset.setIncludesfile(includesfile);
  }

  /** Whether one or more includes or excludes patterns were set. */
  public boolean isIncludesExcludesSet() {
    return this.includesExcludesSet;
  }

  /**
   * If one or more includes or excludes patterns are specified (either as
   * attribute values or in files), the root directory specified by the
   * {@code dir} attribute is recursively scanned. If the recursive scan
   * yields matches, then the directory tree starting with the specified
   * root directory is analyzed to determine common base directories. Only
   * the common base directories are included in the returned list. See
   * {@link org.closureant.base.CommonBaseDirectoryTree}.
   *
   * <p>If the recursive scan yields no matches, then the specified root
   * directory is returned as the only list element. If no includes or
   * excludes patterns are specified, then the root directory is returned
   * without being scanned.</p>
   *
   * <p>To get a list of all matching subdirectories beneath the root
   * directory (not just the common base directories) use {@link DirSet}.</p>
   *
   * @return a list of directories
   * @throws BuildException on error
   */
  public List<File> getMatchedDirectories() {
    if (this.fileset.getDir() == null) {
      throw new BuildException("root directory not set");
    }
    List<File> directories = Lists.newArrayList();

    if (!this.isIncludesExcludesSet()) {
      directories.add(this.fileset.getDir());
      return directories;
    }

    DirectoryScanner scanner = this.fileset.getDirectoryScanner();
    String[] relativePaths = scanner.getIncludedDirectories();

    try {
      boolean rootDirectoryMatched = false;

      for (String path : relativePaths) {
        File directory =
            new File(this.fileset.getDir(), path).getCanonicalFile();
        if (!this.fileset.getDir().equals(directory)) {
          directories.add(directory);
        } else {
          rootDirectoryMatched = true;
        }
      }

      directories = FileUtil.getCommonBaseDirectories(directories);
      if (rootDirectoryMatched || directories.isEmpty()) {
        directories.add(this.fileset.getDir());
      }
    } catch (IOException e) {
      throw new BuildException(e);
    }

    return directories;
  }

  /** @inheritDoc */
  public void setProject(Project project) {
    super.setProject(project);
    fileset.setProject(project);
  }

  // Implement the ResourceCollection interface.

  /** @inheritDoc */
  public Iterator<File> iterator() {
    return this.getMatchedDirectories().iterator();
  }

  /** @inheritDoc */
  public int size() {
    return this.getMatchedDirectories().size();
  }

  /** @inheritDoc */
  public boolean isFilesystemOnly() {
    return this.fileset.isFilesystemOnly();
  }

  /**
   * Returns included directories as a list of semicolon-separated paths.
   *
   * @return a {@code String} of included directories
   */
  @Override
  public String toString() {
    return Joiner.on(";").skipNulls().join(this.getMatchedDirectories());
  }
}
