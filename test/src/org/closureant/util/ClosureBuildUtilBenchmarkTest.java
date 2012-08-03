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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.closureant.base.JsClosureSourceFile;
import org.closureant.base.JsClosureSourceFileMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * Benchmark tests for topological sorting algorithms provided by {@link
 * ClosureBuildUtil}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class ClosureBuildUtilBenchmarkTest {


  @Test public void topologicalSortBenchmark() {
    List<JsClosureSourceFile> shortList = Lists.newArrayListWithExpectedSize(100);
    List<JsClosureSourceFile> longList = Lists.newArrayListWithExpectedSize(10000);

    JsClosureSourceFileMock source = new JsClosureSourceFileMock("source1.js");
    source.setProvides(ImmutableList.of("source1"));
    shortList.add(source);
    longList.add(source);

    for (int i = 100; i > 1; i--) {
      source = new JsClosureSourceFileMock("source" + i + ".js");
      source.setProvides(ImmutableList.of("source" + i));
      source.setRequires(ImmutableList.of("source" + (i - 1)));
      shortList.add(source);
      longList.add(source);
    }

    for (int i = 10000; i > 100; i--) {
      source = new JsClosureSourceFileMock("source" + i + ".js");
      source.setProvides(ImmutableList.of("source" + i));
      source.setRequires(ImmutableList.of("source" + (i - 1)));
      longList.add(source);
    }

    long startTimeMs;
    long stopTimeMs;
    long kahnAlgorithmElapsed;
    long depthFirstElapsed;

    // Test topological sort performance on a list of 100 source files.

    Map<String, JsClosureSourceFile> provideToSource = ClosureBuildUtil
        .createMapOfProvideToSource(shortList);
    Multimap<JsClosureSourceFile, JsClosureSourceFile> sourceToDeps = ClosureBuildUtil
        .createSourceToDependencies(shortList, provideToSource);

    startTimeMs = System.currentTimeMillis();

    for (int i = 0; i < 100; i++) {
      ClosureBuildUtil.topologicalStableSortKahnAlgorithm(
          shortList, sourceToDeps);
    }

    stopTimeMs = System.currentTimeMillis();
    kahnAlgorithmElapsed = stopTimeMs - startTimeMs;

    startTimeMs = System.currentTimeMillis();

    for (int i = 0; i < 100; i++) {
      ClosureBuildUtil.topologicalStableSortDepthFirstSearch(
          shortList, sourceToDeps);
    }

    stopTimeMs = System.currentTimeMillis();
    depthFirstElapsed = stopTimeMs - startTimeMs;

    if (kahnAlgorithmElapsed < depthFirstElapsed) {
      Double percentFaster =
          -1.0 * (kahnAlgorithmElapsed - depthFirstElapsed)/depthFirstElapsed;
      System.out.println(
          String.format("Topological sort using Kahn algorithm on 100 source "
              + "files was %3.3f", percentFaster)
              + "% faster than depth-first search.");
    } else {
      Double percentFaster =
          -1.0 * (depthFirstElapsed - kahnAlgorithmElapsed)/kahnAlgorithmElapsed;
      System.out.println(
          String.format("Topological sort using depth-first search on 100 "
              + "source files was %3.3f", percentFaster)
              + "% faster than Kahn algorithm.");
    }

    // Test topological sort performance on a list of 10,000 source files.

    provideToSource = ClosureBuildUtil.createMapOfProvideToSource(longList);
    sourceToDeps = ClosureBuildUtil
        .createSourceToDependencies(longList, provideToSource);

    startTimeMs = System.currentTimeMillis();

    for (int i = 0; i < 10; i++) {
      ClosureBuildUtil.topologicalStableSortKahnAlgorithm(
          longList, sourceToDeps);
    }

    stopTimeMs = System.currentTimeMillis();
    kahnAlgorithmElapsed = stopTimeMs - startTimeMs;

    startTimeMs = System.currentTimeMillis();

    for (int i = 0; i < 10; i++) {
      ClosureBuildUtil.topologicalStableSortDepthFirstSearch(
          longList, sourceToDeps);
    }

    stopTimeMs = System.currentTimeMillis();
    depthFirstElapsed = stopTimeMs - startTimeMs;

    if (kahnAlgorithmElapsed < depthFirstElapsed) {
      Double percentFaster =
          -1.0 * (kahnAlgorithmElapsed - depthFirstElapsed)/depthFirstElapsed;
      System.out.println(
          String.format("Topological sort using Kahn algorithm on 10,000 "
              + "source files was %3.3f", percentFaster)
              + "% faster than using depth-first search.");
    } else {
      Double percentFaster =
          -1.0 * (depthFirstElapsed - kahnAlgorithmElapsed)/kahnAlgorithmElapsed;
      System.out.println(
          String.format("Topological sort using depth-first search on 10,000 "
              + "source files was %3.3f", percentFaster)
              + "% faster than Kahn algorithm.");
    }
  }
}