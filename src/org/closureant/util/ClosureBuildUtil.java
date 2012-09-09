/*
 * Copyright 2010 The Closure Compiler Authors.
 * Copyright (C) 2012 Christopher Peisert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.closureant.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.closureant.base.BuildSettings;
import org.closureant.base.ProvidesRequiresSourceFile;
import org.closureant.deps.CircularDependencyExceptionFactory;
import org.closureant.deps.MissingProvideException;
import org.closureant.deps.MultipleProvideException;

/**
 * Utility class providing functions related to implementing Closure-centric
 * build processes.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ClosureBuildUtil {
  private ClosureBuildUtil() {}

  /**
   * Extract the {@code goog.provided} namespaces from a JavaScript source
   * file.
   *
   * @param sourceFile the JavaScript source file
   * @return a list of {@code goog.provided} namespaces from
   *     {@code sourceFile}. If the {@code sourceFile} does contain any
   *     {@code goog.provided} namespaces, an empty list is returned.
   * @throws IllegalStateException if {@code SourceFile} is Closure's "base.js"
   *     and it {@code goog.provides} one or more namespaces
   * @throws IOException if {@code sourceFile} cannot be read
   * @throws NullPointerException if {@code sourceFile} is null
   */
  public static List<String> extractGoogProvidedNamespaces(File sourceFile)
      throws IOException {
    Preconditions.checkNotNull(sourceFile, "sourceFile was null");

    List<String> provides = Lists.newArrayList();
    String regex = "^\\s*goog\\.provide\\(\\s*['\"](.+)['\"]\\s*\\)";
    Pattern providePattern = Pattern.compile(regex);
    List<String> sourceLines = Files.readLines(sourceFile, Charsets.UTF_8);

    for (String line : sourceLines) {
      Matcher matcher = providePattern.matcher(line);
      if (matcher.find()) {
        provides.add(matcher.group(1));
      }
    }

    if ("base.js".equalsIgnoreCase(sourceFile.getName())) {
      if (isClosureBaseJs(sourceFile)) {
        if (!provides.isEmpty()) {
          throw new IllegalStateException("base.js should not provide or "
              + "require namespaces");
        }
        // Closure's base.js file implicitly provides "goog".
        provides.add("goog");
      }
    }
    return provides;
  }

  /**
   * Create a map of {@code goog.provided} namespaces to the source files in
   * which they are provided.
   *
   * @param sources the JavaScript source files to scan
   * @return a map of {@code goog.provided} namespaces to the source files in
   *     which they are provided
   * @throws MultipleProvideException if a namespace is {@code goog.provided}
   *     by more than one source file
   */
  public static <E extends ProvidesRequiresSourceFile> Map<String, E> 
      createMapOfProvideToSource(Collection<E> sources) {
    
    Map<String, E> provideToSource = Maps.newHashMap();

    for (E sourceFile : sources) {
      Collection<String> currentProvides = sourceFile.getProvides();
      for (String provide : currentProvides) {
        if (provideToSource.containsKey(provide)) {
          throw new MultipleProvideException(provide,
              provideToSource.get(provide), sourceFile);
        }
        provideToSource.put(provide, sourceFile);
      }
    }
    return provideToSource;
  }

  /**
   * Create a multimap from JavaScript source files to their dependencies.
   *
   *
   * @param sources the sources for which to build the dependency multimap
   * @param provideToSource a map of {@code goog.provided} namespaces to the
   *     source files in which they are provided
   * @return a dependency multimap from sources to dependencies
   * @throws MissingProvideException if one of the namespaces {@code
   *     goog.required} by a source file is not found in the {@code
   *     provideToSource} map (i.e. the namespace is never {@code
   *     goog.provided})
   */
  public static <E extends ProvidesRequiresSourceFile> Multimap<E, E>
  createSourceToDependencies(Collection<E> sources,
                             Map<String, E> provideToSource) {

    Multimap<E, E> sourceToDependencies = HashMultimap.create();

    for (E sourceFile : sources) {
      for (String req : sourceFile.getRequires()) {
        E dep = provideToSource.get(req);
        if (dep != null && dep != sourceFile) {
          sourceToDependencies.put(sourceFile, dep);
        }
        if (dep == null) {
          throw new MissingProvideException(req, sourceFile);
        }
      }
    }
    return sourceToDependencies;
  }

  /**
   * Create a multimap from JavaScript source files to their dependencies,
   * ignoring any dependencies that are undefined (i.e. dependencies that are
   * not provided by any of the sources).
   *
   *
   * @param sources the sources for which to build the dependency multimap
   * @param provideToSource a map of {@code goog.provided} namespaces to the
   *     source files in which they are provided
   * @return a dependency multimap from sources to dependencies
   */
  public static <E extends ProvidesRequiresSourceFile> Multimap<E, E>
  createSourceToDependenciesIgnoringMissingDeps(
      Collection<E> sources, Map<String, E> provideToSource) {

    Multimap<E, E> sourceToDependencies = HashMultimap.create();

    for (E sourceFile : sources) {
      for (String req : sourceFile.getRequires()) {
        E dep = provideToSource.get(req);
        if (dep != null && dep != sourceFile) {
          sourceToDependencies.put(sourceFile, dep);
        }
        if (dep == null) {
          // Ignore undefined dependencies.
          // throw new MissingProvideException(req, sourceFile);
        }
      }
    }
    return sourceToDependencies;
  }

  /**
   * Determine if a source file is Closure's base.js.
   * 
   * @param sourceFile the JavaScript source file to test
   * @return {@code true} if the source file is base.js
   * @throws IOException if the source file cannot be read
   */
  public static boolean isClosureBaseJs(File sourceFile) throws IOException {
    Preconditions.checkNotNull(sourceFile, "sourceFile is null");
    if (!"base.js".equalsIgnoreCase(sourceFile.getName())) {
      return false;
    }

    String GOOG_BASE_LINE = "var goog = goog || {}; // Identifies this file "
        + "as the Closure base.";
    List<String> sourceLines = Files.readLines(sourceFile, Charsets.UTF_8);

    for (String line : sourceLines) {
      if (GOOG_BASE_LINE.equals(line.trim())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determine if the source code is Closure's base.js.
   *
   * @param sourceCode the JavaScript source code to test
   * @return {@code true} if the source code is base.js
   */
  public static boolean isClosureBaseJs(String sourceCode) {
    Preconditions.checkNotNull(sourceCode, "sourceCode is null");

    String GOOG_BASE_LINE = "var goog = goog || {}; // Identifies this file "
        + "as the Closure base.";
    // Windows: \r\n Unix: \n Mac: \r
    String[] lines = sourceCode.split("\\r?\\n|\\r");

    for (String line : lines) {
      if (GOOG_BASE_LINE.equals(line.trim())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determine if the {@code outputFile} is up to date.
   *
   * <p>The {@code outputFile} is up to date if the following conditions
   * are satisfied:</p>
   *
   * <p><ol>
   * <li>the output file exists</li>
   * <li>the command line used for the previous build matches the current
   * command line</li>
   * <li>the set of sources from the previous build matches the set of
   * sources for the current build</li>
   * <li>for each source file in the current build, the last modified time
   * precedes the last modified time of the {@code outputFile}
   * </li>
   * </ol></p>
   *
   * @param outputFile the output file
   * @param previousSettings the settings from the previous build
   * @param currentSettings the settings for the current build
   * @return {@code true} if {@code #outputFile} is up-to-date (i.e.
   *     compilation may be skipped)
   */
  public static boolean outputFileUpToDate(File outputFile,
      BuildSettings previousSettings, BuildSettings currentSettings) {
    if (outputFile == null || !outputFile.exists()) {
      return false;
    }
    if (!previousSettings.getCommandLineOrConfig()
        .equals(currentSettings.getCommandLineOrConfig())) {
      return false;
    }

    Set<String> previousSources =
        Sets.newHashSet(previousSettings.getSources());
    Set<String> currentSources = Sets.newHashSet(currentSettings.getSources());
    if (!previousSources.equals(currentSources)) {
      return false;
    }

    Long outputFileTimestamp = outputFile.lastModified();
    for (String filePath : currentSources) {
      if (new File(filePath).lastModified() > outputFileTimestamp) {
        return false;
      }
    }

    return true;
  }

  /**
   * Topologically sort a list of items in a stable order using the
   * <a target="_blank" href="http://en.wikipedia.org/wiki/Topological_sorting">
   * Khan algorithm</a> such that if A comes before B, and A does not
   * transitively depend on B, then A must also come before B in the returned
   * list.
   *
   * @param sources the list of source files to sort
   * @param sourceToDependencies multimap from sources to their direct
   *     dependencies
   * @return a topologically sorted list in stable order
   * @throws org.closureant.deps.CircularDependencyException if
   *     a cycle is formed by the dependency graph of the source files
   */
  public static <E extends ProvidesRequiresSourceFile> List<E> 
      topologicalStableSortKahnAlgorithm(List<E> sources,
          Multimap<E, E> sourceToDependencies) {
    
    if (sources.size() == 0) {
      // Priority queue blows up if we give it a size of 0. Since we need
      // to special case this either way, just bail out.
      return Lists.newArrayList();
    }

    final Map<E, Integer> originalIndex = Maps.newHashMap();
    for (int i = 0; i < sources.size(); i++) {
      originalIndex.put(sources.get(i), i);
    }

    PriorityQueue<E> inDegreeZero = new PriorityQueue<E>(sources.size(), 
        new Comparator<E>() {
          @Override public int compare(E a, E b) {
            return originalIndex.get(a) - originalIndex.get(b);
          }
        });

    List<E> sortedSources = Lists.newArrayList();

    Multiset<E> inDegree = HashMultiset.create();
    Multimap<E, E> reverseDeps =
        ArrayListMultimap.create();
    Multimaps.invertFrom(sourceToDependencies, reverseDeps);

    // First, add all the inputs with in-degree 0.
    for (E sourceFile : sources) {
      Collection<E> sourceDeps =
          sourceToDependencies.get(sourceFile);
      inDegree.add(sourceFile, sourceDeps.size());
      if (sourceDeps.isEmpty()) {
        inDegreeZero.add(sourceFile);
      }
    }

    // Then, iterate to a fixed point over the reverse dependency graph.
    while (!inDegreeZero.isEmpty()) {
      E sourceFile = inDegreeZero.remove();
      sortedSources.add(sourceFile);
      for (E inWaiting : reverseDeps.get(sourceFile)) {
        inDegree.remove(inWaiting, 1);
        if (inDegree.count(inWaiting) == 0) {
          inDegreeZero.add(inWaiting);
        }
      }
    }

    // The dependency graph of inputs has a cycle iff sortedSources is a
    // proper subset of the initial source list.
    if (sortedSources.size() < sources.size()) {
      throw CircularDependencyExceptionFactory
          .newCircularDependencyException(sortedSources, sources);
    }

    return sortedSources;
  }
}
