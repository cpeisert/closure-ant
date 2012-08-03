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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.closureant.base.ProvidesRequiresSourceFile;
import org.closureant.util.ClosureBuildUtil;

/**
 * A builder to create a manifest suitable for the Closure Compiler. Such a
 * manifest is an ordered list of JavaScript source files derived from the
 * transitive dependencies of the program entry points. Program entry points
 * are specified as either namespaces or "main" sources (i.e. source files
 * that must be included in the manifest). The transitive dependencies are
 * defined by calls to {@code goog.provide()} and {@code goog.require()}. A
 * stable topological sort is used to make sure that an input always comes
 * after its dependencies, unless the flag {@link #keepOriginalOrder} is set
 * to {@code true}, in which case the sources are not sorted.
 *
 * <p>Portions of this class were copied from the Closure Compiler class
 * <a target="_blank"
 * href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/deps/SortedDependencies.java">
 * SortedDependencies.java</a>.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 *
 * @param <S> the type of source file being used in the manifest, which must
 *     implement {@link ProvidesRequiresSourceFile}
 */
public final class ManifestBuilder<S extends ProvidesRequiresSourceFile> {

  // List containing all sources (i.e. "main" sources and sources in limbo) in
  // the original order passed to the ManifestBuilder.
  private final List<S> allSourcesInOriginalOrder;

  private boolean keepAllSources;
  private boolean keepMoochers;
  private boolean keepOriginalOrder;

  // The set of sources that are considered application entry points.
  private final Set<S> mainSources;

  // Cached copy of the manifest in the event of multiple calls to
  // toManifestList().
  private List<S> manifest;

  // Flag indicating if the manifest needs to be regenerated, for example,
  // if additional required sources were specified since the manifest was
  // last generated.
  private boolean manifestStale;

  private final Set<String> namespaceEntryPoints;

  // Map of provided namespaces to the corresponding sources where the
  // namespaces are goog.provided.
  private final Map<String, S> provideToSource;

  // Sources that may or may not be required depending on the transitive
  // dependencies of the program entry points.
  private final Set<S> sourcesInLimbo;


  /**
   * Constructs a {@link ManifestBuilder} to generate a JavaScript source
   * manifest for the Closure Compiler.
   */
  public ManifestBuilder() {
    this.allSourcesInOriginalOrder = Lists.newArrayList();
    this.keepAllSources = false;
    this.keepMoochers = false;
    this.keepOriginalOrder = false;
    this.mainSources = Sets.newHashSet();
    this.manifest = null;
    this.manifestStale = true;
    this.namespaceEntryPoints = Sets.newHashSet();
    this.provideToSource = Maps.newHashMap();
    this.sourcesInLimbo = Sets.newHashSet();
  }

  /**
   * Whether all sources should be passed to the Closure Compiler, i.e.,
   * no sources are pruned irrespective of the transitive dependencies of
   * the program entry points.
   *
   * @param keepAllSources whether all sources should be passed to the
   *     Closure Compiler. Defaults to {@code false}.
   * @return this {@link ManifestBuilder}
   */
  public ManifestBuilder<S> keepAllSources(boolean keepAllSources) {
    if (this.keepAllSources != keepAllSources) {
      this.manifestStale = true;
      this.keepAllSources = keepAllSources;
    }
    return this;
  }

  /**
   * Whether "moochers" (i.e. source files that do not provide any namespaces,
   * though they may goog.require namespaces) may be dropped during the
   * dependency pruning process. If {@code true}, these files are always kept
   * as well as any files they depend on. If {@code false}, these files may be
   * dropped during dependency pruning.
   *
   * <p><b>Note:</b> this option has no effect if {@link #keepAllSources} is
   * set to {@code true} (i.e. if dependency pruning is turned off).</p>
   *
   * @param keepMoochers if {@code true}, moochers and their dependencies are
   *     always kept. Defaults to {@code false}.
   * @return this {@link ManifestBuilder}
   */
  public ManifestBuilder<S> keepMoochers(boolean keepMoochers) {
    if (this.keepMoochers != keepMoochers) {
      this.manifestStale = true;
      this.keepMoochers = keepMoochers;
    }
    return this;
  }

  /**
   * Whether sources should be kept in their original order or topologically
   * sorted based on their dependencies.
   *
   * @param keepOriginalOrder if {@code true}, sources will be kept in their
   *     original order, otherwise they will be topologically sorted based on
   *     their dependencies. Defaults to {@code false}.
   * @return this {@link ManifestBuilder}
   */
  public ManifestBuilder<S> keepOriginalOrder(boolean keepOriginalOrder) {
    if (this.keepOriginalOrder != keepOriginalOrder) {
      this.manifestStale = true;
      this.keepOriginalOrder = keepOriginalOrder;
    }
    return this;
  }

  /**
   * Add a source that is an application entry point, i.e., it will not be
   * dropped and its transitive dependencies will be included.
   *
   * @param source the source file
   * @return this {@link ManifestBuilder}
   */
  public ManifestBuilder<S> mainSource(S source) {
    internalAddMainSource(source);
    return this;
  }

  /**
   * Add "main" sources (i.e. application entry points).
   *
   * @param sources the "main" source files to add
   * @return this {@link ManifestBuilder}
   */
  public ManifestBuilder<S> mainSources(Collection<S> sources) {
    for (S source : sources) {
      internalAddMainSource(source);
    }
    return this;
  }

  /**
   * Adds a namespace that is a program entry point. The namespaces must be
   * {@code goog.provided}.
   *
   * @param namespace a Closure namespace
   * @return this {@link ManifestBuilder}
   */
  public ManifestBuilder<S> namespace(String namespace) {
    this.namespaceEntryPoints.add(namespace);
    return this;
  }

  /**
   * Adds namespaces that are program entry points. The namespaces must be
   * {@code goog.provided}.
   *
   * @param namespaces a set of Closure namespaces
   * @return this {@link ManifestBuilder}
   */
  public ManifestBuilder<S> namespaces(Collection<String> namespaces) {
    this.namespaceEntryPoints.addAll(namespaces);
    return this;
  }

  /**
   * Add a source file. The source is placed in "limbo" until
   * {@link ManifestBuilder#toManifestList()} is called, at which time its
   * {@code goog.provided} namespaces are checked against the {@link
   * #namespaceEntryPoints}. If one of the source file's provided namespaces
   * is a program entry point, the source is moved to the set of "main"
   * sources. Otherwise the source is kept in limbo and may be pruned unless
   * it is a transitive dependency of one of the "main" sources or {@link
   * #keepAllSources} is set to {@code true}.
   *
   * @param source the source file
   * @return this {@link ManifestBuilder}
   */
  public ManifestBuilder<S> source(S source)
  {
    putSourceInLimbo(source);
    return this;
  }

  /**
   * Add source files. See {@link
   * ManifestBuilder#source(ProvidesRequiresSourceFile)}.
   *
   * @param sources the source files to add
   * @return this {@link ManifestBuilder}
   */
  public ManifestBuilder<S> sources(Collection<S> sources) {
    for (S source : sources) {
      putSourceInLimbo(source);
    }
    return this;
  }

  /**
   * Builds the manifest and returns it as a list of sources.
   *
   * @return a {@link List} containing the transitive closure of the main
   *     sources sorted in topological order
   * @throws CircularDependencyException if the goog.provided and goog.required
   *     namespaces form a cycle
   * @throws MissingProvideException if a goog.required namespace is not
   *     goog.provided by any of the inputs
   * @throws MultipleProvideException if a namespace is provided by more than
   *     one source file
   */
  public List<S> toManifestList() {
    if (!this.manifestStale) {
      if (this.manifest != null) {
        return this.manifest;
      } else {
        throw new IllegalStateException("manifestStale is false but the "
            + "cached manifest is null");
      }
    }

    List<S> prunedSources;
    
    if (!this.keepAllSources) {
      if (this.keepMoochers) {
        // Note: a for-each loop will not work here because elements are
        // removed from the list being iterated.
        for (Iterator<S> i = this.sourcesInLimbo.iterator(); i.hasNext(); ) {
          S candidateMoocher = i.next();
          if (candidateMoocher.getProvides().isEmpty()) {
            this.mainSources.add(candidateMoocher);
            i.remove(); // Delete candidateMoocher from sourcesInLimbo.
          }
        }
      }
      processNamespaceEntryPoints();
      prunedSources = getDependenciesOf(this.mainSources, this.provideToSource);
    } else {
      prunedSources = this.allSourcesInOriginalOrder;
    }

    if (!this.keepOriginalOrder) {
      // Get the direct dependencies.
      final Multimap<S, S> sourceToDependencies =
          ClosureBuildUtil.createSourceToDependencies(prunedSources,
              this.provideToSource);

      // Topologically sort the pruned sources.
      this.manifest = ClosureBuildUtil.topologicalStableSortDepthFirstSearch(
          prunedSources, sourceToDependencies);
    } else {
      this.manifest = prunedSources;
    }
    this.manifestStale = false;
    return this.manifest;
  }

  /**
   * Get all sources added to the {@link ManifestBuilder} (both "main" sources
   * and other sources) in their original order.
   *
   * @return all sources in their original order (i.e. prior to dependency
   *     management)
   */
  public List<S> getAllSourcesInOriginalOrder() {
    return ImmutableList.copyOf(this.allSourcesInOriginalOrder);
  }

  /**
   * Add a "main" source and update data structures to maintain class
   * invariants. For example, whenever a new source is added, the flag
   * {@link #manifestStale} must be set to {@code true}.
   *
   * @param sourceFile a new "main" source file (i.e. a program entry point)
   */
  private void internalAddMainSource(S sourceFile) {
    if (!this.mainSources.contains(sourceFile)) {
      this.mainSources.add(sourceFile);
      this.manifestStale = true;
      
      if (!this.sourcesInLimbo.contains(sourceFile)) {
        internalAddConfirmedNewSource(sourceFile);
      } else {
        this.sourcesInLimbo.remove(sourceFile);
      }
    }
  }

  /**
   * Puts a source file in a state of limbo (i.e. the source file may be a
   * "main" source, a transitive dependency of the "main" sources, or dropped
   * through dependency pruning).
   *
   * @param sourceFile a source file to place in limbo
   */
  private void putSourceInLimbo(S sourceFile) {
    if (!this.sourcesInLimbo.contains(sourceFile)
        && !this.mainSources.contains(sourceFile)) {

      // If the source is Closure's base.js, it needs to be a program entry
      // point to ensure that it is not dropped during dependency pruning so
      // that the Closure Library primitives are defined.
      if (ClosureBuildUtil.isClosureBaseJs(sourceFile.getCode())) {
        this.mainSources.add(sourceFile);
      } else {
        this.sourcesInLimbo.add(sourceFile);
      }
      this.manifestStale = true;
      internalAddConfirmedNewSource(sourceFile);
    }
  }

  /**
   * Once a source file has been confirmed to be a new source (i.e. not
   * already contained in {@link #mainSources} or {@link #sourcesInLimbo}), 
   * this method ensures that the master list of sources {@link 
   * #allSourcesInOriginalOrder} is updated as well as the map of {@code 
   * goog.provided} namespaces to source files {@link #provideToSource}.
   *
   * @param sourceFile the confirmed new source file
   */
  private void internalAddConfirmedNewSource(S sourceFile) {
    this.allSourcesInOriginalOrder.add(sourceFile);   
    
    Collection<String> currentProvides = sourceFile.getProvides();     
    for (String provide : currentProvides) {
      if (this.provideToSource.containsKey(provide)) {
        throw new MultipleProvideException(provide,
            this.provideToSource.get(provide), sourceFile);
      }
      this.provideToSource.put(provide, sourceFile);
    }
  }

  /**
   * Process the namespace entry points by ensuring that they are all {@code
   * goog.provided} and that the sources that provide the namespaces are all
   * members of {@link #mainSources} instead of {@link #sourcesInLimbo}.
   */
  private void processNamespaceEntryPoints() {
    for (String namespace : this.namespaceEntryPoints) {
      if (this.provideToSource.containsKey(namespace)) {
        S mainSource = this.provideToSource.get(namespace);
        if (this.sourcesInLimbo.contains(mainSource)) {
          this.sourcesInLimbo.remove(mainSource);
          this.mainSources.add(mainSource);
        } else if (!this.mainSources.contains(mainSource)) {
          throw new IllegalStateException("Source \""
              + mainSource.getAbsolutePath() + "\" is contained in map "
              + "provideToSource, but is neither contained in "
              + "sourcesInLimbo nor mainSources.");
        }
      } else {
        throw new MissingProvideException(namespace, "namespace \""
            + namespace + "\" is a namespace entry point but is never "
            + "goog.provided.");
      }
    }
    // Since the source file entry points for each namespace are now in
    // mainSources, there is no need to track them separately.
    this.namespaceEntryPoints.clear();
  }

  /**
   * Gets all the dependencies of the given program entry points.
   *
   * @param mainSources collection of sources that are considered application
   *     entry points
   * @param provideToSource map of provided namespaces to the corresponding
   *     sources where the namespaces are {@code goog.provided}
   */
  private List<S> getDependenciesOf(Collection<S> mainSources,
      Map<String, S> provideToSource) {
    Set<S> included = Sets.newHashSet();
    Deque<S> worklist = new ArrayDeque<S>(mainSources);
    while (!worklist.isEmpty()) {
      S current = worklist.pop();
      if (included.add(current)) {
        for (String req : current.getRequires()) {
          S dep = provideToSource.get(req);
          if (dep != null) {
            worklist.add(dep);
          }
        }
      }
    }

    ImmutableList.Builder<S> builder =
        ImmutableList.builder();
    for (S current : this.allSourcesInOriginalOrder) {
      if (included.contains(current)) {
        builder.add(current);
      }
    }
    return builder.build();
  }
}
