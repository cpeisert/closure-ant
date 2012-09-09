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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.closureant.util.AntUtil}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class ClosureBuildUtilTest {

  private static File sourceFile;
  private static File baseJs;

  @BeforeClass public static void suiteSetup() {
    sourceFile = new File("./tmp.js");
    baseJs = new File("./base.js");
    try {
      sourceFile.createNewFile();
      baseJs.createNewFile();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @AfterClass public static void suiteTearDown() {
    sourceFile.delete();
    baseJs.delete();
  }

  @Test public void extractGoogProvidedNamespaces_NoNamespace()
      throws IOException {
    Files.write("var answer = 42; alert(answer);", sourceFile, Charsets.UTF_8);
    List<String> expectedNamespaces = ImmutableList.of();
    List<String> actualNamespaces =
        ClosureBuildUtil.extractGoogProvidedNamespaces(sourceFile);
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test public void extractGoogProvidedNamespaces_OneNamespace()
      throws IOException {
    Files.write("goog.provide('my.namespace');", sourceFile, Charsets.UTF_8);
    List<String> expectedNamespaces = ImmutableList.of("my.namespace");
    List<String> actualNamespaces =
        ClosureBuildUtil.extractGoogProvidedNamespaces(sourceFile);
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test public void extractGoogProvidedNamespaces_TwoNamespaces()
      throws IOException {
    Files.write("goog.provide('my.namespace');" + String.format("%n")
        + "goog.provide('far.out');", sourceFile, Charsets.UTF_8);
    List<String> expectedNamespaces = ImmutableList.of("my.namespace",
        "far.out");
    List<String> actualNamespaces =
        ClosureBuildUtil.extractGoogProvidedNamespaces(sourceFile);
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test public void extractGoogProvidedNamespaces_ValidBaseJs()
      throws IOException {
    Files.write("var goog = goog || {}; // Identifies this file "
        + "as the Closure base.", baseJs, Charsets.UTF_8);
    List<String> expectedNamespaces = ImmutableList.of("goog");
    List<String> actualNamespaces =
        ClosureBuildUtil.extractGoogProvidedNamespaces(baseJs);
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test(expected = IllegalStateException.class) public void
  extractGoogProvidedNamespaces_ValidBaseJsProvidesAdditionalNamespace()
      throws IOException {
    Files.write("var goog = goog || {}; // Identifies this file as the "
        + "Closure base." + String.format("%n")
        + "goog.provide('illegal.namespace.in.basejs');", baseJs,
        Charsets.UTF_8);
    ClosureBuildUtil.extractGoogProvidedNamespaces(baseJs);
  }

  @Test public void extractGoogProvidedNamespaces_InvalidBaseJs()
      throws IOException {
    Files.write("var goog_is_undefined = {};",
        baseJs, Charsets.UTF_8);
    List<String> expectedNamespaces = ImmutableList.of();
    List<String> actualNamespaces =
        ClosureBuildUtil.extractGoogProvidedNamespaces(baseJs);
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test public void isBaseJs_ValidBaseJs() throws IOException {
    Files.write("var goog = goog || {}; // Identifies this file "
        + "as the Closure base.", baseJs, Charsets.UTF_8);
    assertTrue(ClosureBuildUtil.isClosureBaseJs(baseJs));
  }

  @Test public void isBaseJs_WrongFileName() throws IOException {
    Files.write("var goog = goog || {}; // Identifies this file "
        + "as the Closure base.", sourceFile, Charsets.UTF_8);
    assertFalse(ClosureBuildUtil.isClosureBaseJs(sourceFile));
  }

  @Test public void isBaseJs_WrongContent() throws IOException {
    Files.write("var goog = 42;", baseJs, Charsets.UTF_8);
    assertFalse(ClosureBuildUtil.isClosureBaseJs(baseJs));
  }
}
