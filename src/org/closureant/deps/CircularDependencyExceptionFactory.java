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

package org.closureant.deps;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.closureant.base.ProvidesRequiresSourceFile;
import org.closureant.util.ClosureBuildUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static factory class to create new instances of {@link
 * CircularDependencyException}.
 *
 * @author nicksantos@google.com (Nick Santos)
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class CircularDependencyExceptionFactory {
  private CircularDependencyExceptionFactory() {}

  /**
   * Create a new instance of {@link CircularDependencyException}.
   *
   * @param sortedSources a Collection of topologically sorted sources
   * @param originalSources a Collection of the original sources just prior
   *     to being topologically sorted
   * @return a new instance of {@link CircularDependencyException} with a
   *     detail message showing the cycle
   */
  public static <E extends ProvidesRequiresSourceFile> 
      CircularDependencyException newCircularDependencyException(
          Collection<E> sortedSources, Collection<E> originalSources) {

    Map<String, E> provideToSource =
        ClosureBuildUtil.createMapOfProvideToSource(originalSources);

    // The dependency graph of inputs has a cycle iff sortedSources is a
    // proper subset of managedSources. Also, it has a cycle iff the subgraph
    // (managedSources - sortedSources) has a cycle. It's fairly easy to
    // prove this by the lemma that a graph has a cycle iff it has a
    // subgraph where no nodes have out-degree 0. I'll leave the proof of
    // this as an exercise to the reader.
    List<E> subgraph =
        Lists.newArrayList(originalSources);
    subgraph.removeAll(sortedSources);
    String message = cycleToString(findCycle(subgraph, provideToSource));

    return new BasicCircularDependencyException(message);
  }

  /**
   * Returns the first circular dependency found. Expressed as a list of
   * items in reverse dependency order (the second element depends on the
   * first, etc.).
   */
  private static <E extends ProvidesRequiresSourceFile> List<E> findCycle(
      List<E> subGraph, Map<String, E> provideToSource) {
    
    return findCycle(subGraph.get(0), Sets.<E>newHashSet(subGraph),
        provideToSource, Sets.<E>newHashSet());
  }

  private static <E extends ProvidesRequiresSourceFile> List<E> findCycle(
      E current, Set<E> subGraph, Map<String, E> provideToSource,
      Set<E> covered) {

    if (covered.add(current)) {
      List<E> cycle = findCycle(
          findRequireInSubGraphOrFail(current, subGraph, provideToSource),
          subGraph, provideToSource, covered);

      // Don't add the input to the list if the cycle has closed already.
      if (cycle.get(0) != cycle.get(cycle.size() - 1)) {
        cycle.add(current);
      }

      return cycle;
    } else {
      // Explicitly use the add() method, to prevent a generics constructor
      // warning that is dumb. The condition it's protecting is
      // obscure, and I think people have proposed that it be removed.
      List<E> cycle = Lists.newArrayList();
      cycle.add(current);
      return cycle;
    }
  }

  private static <E extends ProvidesRequiresSourceFile> E
      findRequireInSubGraphOrFail(E input, Set<E> subGraph,
          Map<String, E> provideToSource) {

    for (String namespace : input.getRequires()) {
      E candidate = provideToSource.get(namespace);
      if (subGraph.contains(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("no require found in subgraph");
  }

  /**
   * @param cycle A cycle in reverse-dependency order.
   */
  private static <E extends ProvidesRequiresSourceFile> String cycleToString(
      List<E> cycle) {
    List<String> namespaces = Lists.newArrayList();
    for (int i = cycle.size() - 1; i >= 0; i--) {
      namespaces.add(cycle.get(i).getProvides().iterator().next());
    }
    namespaces.add(namespaces.get(0));
    return Joiner.on(" -> ").join(namespaces);
  }

  /**
   * Basic implementation of {@link CircularDependencyException}.
   */
  private static class BasicCircularDependencyException
      extends CircularDependencyException {

    private static final long serialVersionUID = 0L;

    BasicCircularDependencyException(String message) {
      super(message);
    }
  }
}