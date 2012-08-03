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

package org.closureant.deps;

import com.google.common.collect.ImmutableList;

import org.closureant.base.JsClosureSourceFileMock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ManifestBuilder}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class ManifestBuilderTest {

  private static JsClosureSourceFileMock source1;
  private static JsClosureSourceFileMock source2;
  private static JsClosureSourceFileMock source3;
  private static JsClosureSourceFileMock providesSource3;
  private static JsClosureSourceFileMock moocher;
  private static JsClosureSourceFileMock baseJs;
  private static JsClosureSourceFileMock cycle1;
  private static JsClosureSourceFileMock cycle2;
  private static JsClosureSourceFileMock cycle3;

  @BeforeClass
  public static void suiteSetup() {
    source1 = new JsClosureSourceFileMock("source1.js");
    source1.setProvides(ImmutableList.of("source1"));
    source1.setRequires(ImmutableList.of("goog"));

    source2 = new JsClosureSourceFileMock("source2.js");
    source2.setProvides(ImmutableList.of("source2"));
    source2.setRequires(ImmutableList.of("source1"));

    source3 = new JsClosureSourceFileMock("source3.js");
    source3.setProvides(ImmutableList.of("source3"));
    source3.setRequires(ImmutableList.of("source2"));

    providesSource3 = new JsClosureSourceFileMock("providesSource3.js");
    providesSource3.setProvides(ImmutableList.of("source3"));

    moocher = new JsClosureSourceFileMock("moocher.js");
    moocher.setRequires(ImmutableList.of("goog"));

    baseJs = new JsClosureSourceFileMock("base.js");
    baseJs.setBaseJs(true);
    baseJs.setProvides(ImmutableList.of("goog"));

    cycle1 = new JsClosureSourceFileMock("cycle1.js");
    cycle1.setProvides(ImmutableList.of("cycle1"));
    cycle1.setRequires(ImmutableList.of("cycle3"));

    cycle2 = new JsClosureSourceFileMock("cycle2.js");
    cycle2.setProvides(ImmutableList.of("cycle2"));
    cycle2.setRequires(ImmutableList.of("cycle1"));

    cycle3 = new JsClosureSourceFileMock("cycle3.js");
    cycle3.setProvides(ImmutableList.of("cycle3"));
    cycle3.setRequires(ImmutableList.of("cycle2"));
  }

  @Test public void defaultSettings() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(baseJs, source1);
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.mainSource(source1);
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    assertEquals(expected, builder.toManifestList());
  }

  @Test public void keepAllSources() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(baseJs, source1, source2, source3);
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.keepAllSources(true);
    builder.mainSource(source1);
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    assertEquals(expected, builder.toManifestList());
  }

  @Test public void keepMoochers() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(baseJs, source1, moocher);
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.keepMoochers(true);
    builder.mainSource(source1);
    builder.source(moocher);
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    assertEquals(expected, builder.toManifestList());
  }

  @Test public void dropMoochers() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(baseJs, source1);
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.mainSource(source1);
    builder.source(moocher);
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    assertEquals(expected, builder.toManifestList());
  }

  @Test public void addMoocherToMainSources() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(baseJs, moocher, source1);
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.mainSource(moocher);
    builder.mainSource(source1);
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    assertEquals(expected, builder.toManifestList());
  }

  @Test public void keepOriginalOrder() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(source1, baseJs);
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.keepOriginalOrder(true);
    builder.mainSource(source1);
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    assertEquals(expected, builder.toManifestList());
  }

  @Test public void mainSources() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(baseJs, source1);
    List<JsClosureSourceFileMock> mainSources =
        ImmutableList.of(source1, baseJs);
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.mainSources(mainSources);
    builder.source(source3);
    builder.source(source2);
    assertEquals(expected, builder.toManifestList());
  }

  @Test public void namespace() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(baseJs, source1);
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.namespace("source1");
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    builder.source(source1);
    assertEquals(expected, builder.toManifestList());
  }

  @Test public void namespaces() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(baseJs, source1);
    List<String> namespaces = ImmutableList.of("source1", "goog");
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.namespaces(namespaces);
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    builder.source(source1);
    assertEquals(expected, builder.toManifestList());
  }

  @Test public void sources() {
    List<JsClosureSourceFileMock> expected =
        ImmutableList.of(baseJs, source1);
    List<JsClosureSourceFileMock> sourceList =
        ImmutableList.of(source2, source3, baseJs);
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.mainSource(source1);
    builder.sources(sourceList);
    assertEquals(expected, builder.toManifestList());
  }

  @Test(expected = CircularDependencyException.class)
  public void circularDependencyException() {
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.mainSource(cycle1);
    builder.source(cycle2);
    builder.source(cycle3);
    builder.toManifestList();
  }

  @Test(expected = MissingProvideException.class)
  public void missingProvideException() {
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.namespace("not.provided");
    builder.mainSource(source1);
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    builder.toManifestList();
  }

  @Test(expected = MultipleProvideException.class)
  public void multipleProvideException() {
    ManifestBuilder<JsClosureSourceFileMock> builder =
        new ManifestBuilder<JsClosureSourceFileMock>();
    builder.mainSource(source1);
    builder.source(providesSource3);
    builder.source(source3);
    builder.source(baseJs);
    builder.source(source2);
    builder.toManifestList();
  }
}