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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import org.closureant.base.JsClosureSourceFile;
import org.closureant.base.SourceFileFactory;
import org.closureant.deps.DirectoryPathPrefixPair;
import org.closureant.deps.FilePathDepsPathPair;
import org.closureant.util.AntUtil;
import org.closureant.util.FileUtil;

/**
 * DepsWriter Ant task. This task provides a Java implementation to generate
 * deps files rather than the original Python script "depswriter.py" (located
 * in  closure-library/closure/bin/build). The default task name is {@code
 * deps-writer} as defined in "task-definitions.xml". To use the original
 * depswriter python script, see {@link DepsWriterPython}.
 *
 * <p>For more information about DepsWriter, see
 * <a target="_blank"
 * href="https://developers.google.com/closure/library/docs/depswriter">
 * Using DepsWriter</a>.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class DepsWriter extends Task {

  // Attributes
  private File outputFile;

  // Nested elements
  private final List<FilePathDepsPathPair> paths;
  private final List<DirectoryPathPrefixPair> roots;

  /**
   * Constructs a new Ant task for Deps Writer.
   */
  public DepsWriter() {
    // Attributes
    this.outputFile = null;

    // Nested elements
    this.paths = Lists.newArrayList();
    this.roots = Lists.newArrayList();
  }

  // Attribute setters

  /** @param file the file to write output to instead of standard output */
  public void setOutputFile(File file) {
    this.outputFile = file;
  }

  // Nested element setters

  /**
   * @param path a file path and an optional alternate path to the file
   *     in the generated deps file
   */
  public void addPath(FilePathDepsPathPair path) {
    this.paths.add(path);
  }

  /**
   * @param root a root path to scan for JavaScript source files and an
   *     optional prefix to use in the deps file
   */
  public void addRoot(DirectoryPathPrefixPair root) {
    this.roots.add(root);
  }

  /**
   * @param sourceFiles source files to add to the deps file
   */
  public void addConfiguredSources(FileSet sourceFiles) {
    List<File> files = AntUtil.getListOfFilesFromAntFileSet(getProject(),
        sourceFiles);
    for (File file : files) {
      this.paths.add(new FilePathDepsPathPair(file.getAbsolutePath(), null));
    }
  }

  /**
   * Execute the Deps Writer task.
   *
   * @throws BuildException on error.
   */
  @Override
  public void execute() {

    Map<String, JsClosureSourceFile> pathToJsInput = createPathToJsInputMap();
    String googAddDepsCalls = prepareGoogAddDependencyCalls(pathToJsInput);

    String header = "// This file was autogenerated by " + getTaskName() + "."
        + String.format("%n") + "// Please do not edit." + String.format("%n");

    if (this.outputFile != null) {
      try {
        Files.write(header + googAddDepsCalls, this.outputFile, Charsets.UTF_8);
      } catch (IOException e) {
        throw new BuildException(e);
      }
    } else {
      log(header + googAddDepsCalls);
    }
  }

  /**
   * Create a map of input file paths to their JavaScript input files based
   * on the {@link #paths} and {@link #roots} set in this task.
   *
   * @return map of input file paths to their JavaScript input files
   * @throws BuildException on error
   */
  private Map<String, JsClosureSourceFile> createPathToJsInputMap() {
    Map<String, JsClosureSourceFile> map = Maps.newTreeMap();

    for (FilePathDepsPathPair pair : this.paths) {
      if (pair.getFilePath() == null) {
        throw new BuildException("null file path");
      }
      JsClosureSourceFile input;

      try {
        input = SourceFileFactory.newJsClosureSourceFile(
            new File(pair.getFilePath()));
      } catch (IOException e) {
        throw new BuildException(e);
      }

      if (pair.getDepsPath() != null) {
        map.put(normalizePath(pair.getDepsPath()), input);
      } else {
        map.put(normalizePath(pair.getFilePath()), input);
      }
    }

    for (DirectoryPathPrefixPair dirPrefixPair : roots) {
      if (dirPrefixPair.getDirPath() == null) {
        throw new BuildException("null root directory path");
      }

      String prefix = (dirPrefixPair.getPrefix() != null) ?
          dirPrefixPair.getPrefix() : "";
      List<String> relativePaths = FileUtil.scanDirectoryRelativePaths(
          new File(dirPrefixPair.getDirPath()),
          /* includes */ ImmutableList.of("**/*.js"),
          /* excludes */ ImmutableList.of(".*"));

      try {
        for (String relativePath : relativePaths) {
          JsClosureSourceFile file = SourceFileFactory.newJsClosureSourceFile(
              new File(dirPrefixPair.getDirPath(), relativePath));
          map.put(normalizePath(new File(prefix, relativePath).getPath()),file);
        }
      } catch (IOException e) {
        throw new BuildException(e);
      }
    }
    return map;
  }

  /**
   * Prepare the deps file contents comprised of calls to
   * {@code goog.addDependency()} separated by newline characters.
   *
   * @param pathToJsInput map of deps paths to input files
   * @return the {@code goog.addDependency()} calls for the deps file
   */
  private String prepareGoogAddDependencyCalls(
      Map<String, JsClosureSourceFile> pathToJsInput) {
    StringBuilder depsBuilder = new StringBuilder(pathToJsInput.size());

    for (Map.Entry<String, JsClosureSourceFile> pair : pathToJsInput.entrySet()) {
      JsClosureSourceFile input = pair.getValue();
      // Skip entries that do not goog.provide() anything.
      if (!input.getProvides().isEmpty()) {
        depsBuilder.append("goog.addDependency('");
        depsBuilder.append(pair.getKey()).append("', ");
        depsBuilder.append(collectionOfStringToString(input.getProvides()));
        depsBuilder.append(", ");
        depsBuilder.append(collectionOfStringToString(input.getRequires()));
        depsBuilder.append(");").append(String.format("%n"));
      }
    }
    return depsBuilder.toString();
  }

  /**
   * Create a string representation of a Collection of String formatted as:
   *
   * <p>{@code ['<element 1>', '<element 2>', ...]}</p>
   *
   * @param collection the Collection of Strings
   * @return a string representation of the Collection
   */
  private String collectionOfStringToString(Collection<String> collection) {
    StringBuilder builder = new StringBuilder();

    builder.append("[");
    for (String string : collection) {
      builder.append("'").append(string).append("', ");
    }
    if (builder.length() > 2) {
      // delete trailing ", "
      builder.delete(builder.length() - 2, builder.length());
    }
    builder.append("]");
    return builder.toString();
  }

  /**
   * Checks if a path is a relative path, and if so, ensures there is no
   * leading file separator and converts backslashes to forward slashes.
   * Forward slashes are recognized as valid file separators on Windows,
   * Linux, and Mac.
   *
   * @param path the path to check
   * @return the path without a leading separator if the path is relative and
   *     any forward slashes converted to backslashes
   */
  private String normalizePath(String path) {
    File file = new File(path);
    if (!file.exists()) {
      if (path.startsWith(File.separator)) {
        path = path.substring(1, path.length());
      }
    }
    return path.replace('\\', '/');
  }
}