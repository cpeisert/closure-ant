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

package org.closureant.util;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant utility class.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class AntUtil {
  private AntUtil() {}

  /**
   * Creates a {@link List} of the files contained in an Ant {@link FileSet}
   * or {@link DirSet}.
   * 
   * @param project the Ant project
   * @param fileSet an Ant {@link FileSet} or {@link DirSet}
   * @return a list of the files contained in {@code fileSet}
   * @throws IllegalArgumentException if the runtime type of {@code fileSet} 
   *     is not {@link FileSet} or {@link DirSet}
   * @throws BuildException if there is an {@link IOException} attempting to
   *     get the canonical file path
   */
  public static List<File> getListOfFilesFromAntFileSet(Project project,
      AbstractFileSet fileSet) {
    List<File> files = Lists.newArrayList();
    DirectoryScanner scanner = fileSet.getDirectoryScanner(project);
    String[] relativePaths;
    
    if (fileSet instanceof FileSet) {    
      relativePaths = scanner.getIncludedFiles();
    } else if (fileSet instanceof DirSet) {
      relativePaths = scanner.getIncludedDirectories();
    } else {
      throw new IllegalArgumentException("runtime type of AbstractFileSet[" 
          + fileSet.getClass() + "] not recognized");
    }
    
    for (String path : relativePaths) {
      try {
        files.add(new File(fileSet.getDir(project), path).getCanonicalFile());
      } catch (IOException e) {
        throw new BuildException(e);
      }
    }
    return files;
  }

  /**
   * Creates a {@link List} of the relative file paths for the files contained
   * in an Ant {@link FileSet} or {@link DirSet}.
   *
   * @param project the Ant project
   * @param fileSet an Ant {@link FileSet} or {@link DirSet}
   * @return a list of the relative file paths contained in {@code fileSet}
   * @throws IllegalArgumentException if the runtime type of {@code fileSet}
   *     is not {@link FileSet} or {@link DirSet}
   */
  public static List<String> getListOfRelativeFilePathsFromAntFileSet(
      Project project, AbstractFileSet fileSet) {
    DirectoryScanner scanner = fileSet.getDirectoryScanner(project);
    String[] relativePaths;

    if (fileSet instanceof FileSet) {
      relativePaths = scanner.getIncludedFiles();
    } else if (fileSet instanceof DirSet) {
      relativePaths = scanner.getIncludedDirectories();
    } else {
      throw new IllegalArgumentException("runtime type of AbstractFileSet["
          + fileSet.getClass() + "] not recognized");
    }

    return Arrays.asList(relativePaths);
  }

  /**
   * Creates a {@link List} of the file paths contained in a {@link Collection}
   * of {@link FileSet}.
   *
   * @param project the Ant project
   * @param fileSets a collection of FileSets
   * @return a list of file paths
   */
  public static <T extends AbstractFileSet> List<String>
  getFilePathsFromCollectionOfFileSet(Project project, Collection<T> fileSets) {
    List<String> filePaths = Lists.newArrayList();

    for (T fileSet : fileSets) {
      List<File> files = AntUtil.getListOfFilesFromAntFileSet(project, fileSet);
      for (File file : files) {
        filePaths.add(file.getAbsolutePath());
      }
    }
    return filePaths;
  }
}
