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
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;

import org.closureant.base.CommonBaseDirectoryTree;

/**
 * Utility class providing a collection of functions for specialized file
 * applications.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class FileUtil {
  private FileUtil() {}

  /**
   * Deletes files older than {@code days} days from {@code directory}
   * matching {@link org.apache.tools.ant.DirectoryScanner} includes pattern
   * {@code filePattern}.
   *
   * @param directory the directory to search
   * @param filePattern an Ant {@link DirectoryScanner} includes pattern
   * @param days number of days after which files are deleted
   */
  public static void deleteFilesOlderThanNumberOfDays(File directory,
      String filePattern, int days) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, days * -1);
    long purgeTime = calendar.getTimeInMillis();

    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setIncludes(new String[]{filePattern});
    scanner.setBasedir(directory.getAbsolutePath());
    scanner.scan();
    String[] relativePaths = scanner.getIncludedFiles();

    for (String path : relativePaths) {
      File file = new File(directory, path);
      if (file.lastModified() < purgeTime) {
        file.delete();
      }
    }
  }

  /**
   * Given a list of directory paths, removes paths that are subdirectories of
   * other paths in the list. This is useful when passing root directories to
   * programs for recursive scanning to avoid redundancy, which in some
   * cases can cause programs run out of memory. See {@link
   * CommonBaseDirectoryTree}.
   *
   * @param directories a list of directory paths to prune for unique base
   *     directories
   * @return list of unique base directories
   * @throws IOException if the canonical path cannot be obtained for a
   *     directory
   */
  public static List<File> getCommonBaseDirectories(
      Collection<File> directories) throws IOException {

    CommonBaseDirectoryTree commonBaseDirectoryTree =
        new CommonBaseDirectoryTree(directories);
    return commonBaseDirectoryTree.getCommonBaseDirectories();
  }

  /**
   * Wrapper for {@link DirectoryScanner} to simplify usage.
   *
   * @param dir the directory to scan
   * @param includePatterns a collection of include patterns
   * @param excludePatterns a collection of exclude patterns
   * @return a list of files that matched the include patterns and did not
   *     match the exclude patterns
   * @throws IOException if the canonical path is unattainable for one of the
   *     matching files
   * @see DirectoryScanner
   */
  public static List<String> scanDirectory(File dir,
      Collection<String> includePatterns, Collection<String> excludePatterns)
      throws IOException {

    DirectoryScanner scanner = new DirectoryScanner();
    if (includePatterns != null) {
      scanner.setIncludes(includePatterns.toArray(new String[0]));
    }
    if (excludePatterns != null) {
      scanner.setExcludes(excludePatterns.toArray(new String[0]));
    }

    scanner.setBasedir(dir.getAbsolutePath());
    scanner.scan();
    String[] relativePaths = scanner.getIncludedFiles();
    List<String> scannedFiles = Lists.newArrayList();

    for (String path : relativePaths) {
      scannedFiles.add(new File(dir, path).getCanonicalPath());
    }

    return scannedFiles;
  }

  /**
   * Wrapper for {@link DirectoryScanner} to simplify usage that returns
   * relative paths to files that match the include patterns and do not match
   * the exclude patterns.
   *
   * @param dir the directory to scan
   * @param includePatterns a collection of include patterns
   * @param excludePatterns a collection of exclude patterns
   * @return a list of relative file paths to files that matched the include
   *     patterns and did not match the exclude patterns
   * @see DirectoryScanner
   */
  public static List<String> scanDirectoryRelativePaths(File dir,
      Collection<String> includePatterns, Collection<String> excludePatterns) {

    DirectoryScanner scanner = new DirectoryScanner();
    if (includePatterns != null) {
      scanner.setIncludes(includePatterns.toArray(new String[0]));
    }
    if (excludePatterns != null) {
      scanner.setExcludes(excludePatterns.toArray(new String[0]));
    }

    scanner.setBasedir(dir.getAbsolutePath());
    scanner.scan();
    String[] relativePaths = scanner.getIncludedFiles();

    return Arrays.asList(relativePaths);
  }
}
