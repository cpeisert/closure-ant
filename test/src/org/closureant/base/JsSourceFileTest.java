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

package org.closureant.base;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.closureant.base.JsClosureSourceFile}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class JsSourceFileTest {

  private static File sourceFile;
  private static File baseJs;

  @BeforeClass
  public static void suiteSetup() {
    sourceFile = new File("./tmp.js");
    baseJs = new File("./base.js");
    try {
      sourceFile.createNewFile();
      baseJs.createNewFile();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @AfterClass
  public static void suiteTearDown() {
    sourceFile.delete();
    baseJs.delete();
  }

  @Test public void inputWithNoProvidedRequiredNamespaces() throws IOException {
    Files.write("var answer = 42; alert(answer);", sourceFile, Charsets.UTF_8);
    Collection<String> expectedNamespaces = ImmutableSet.of();
    Collection<String> actualNamespaces = SourceFileFactory.newJsClosureSourceFile
        (sourceFile).getProvides();
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test public void inputProvidesOneNamespace() throws IOException {
    Files.write("goog.provide('my.namespace');", sourceFile, Charsets.UTF_8);
    Collection<String> expectedNamespaces = ImmutableSet.of("my.namespace");
    Collection<String> actualNamespaces = SourceFileFactory.newJsClosureSourceFile(sourceFile).getProvides();
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test public void inputProvidesTwoNamespaces() throws IOException {
    Files.write("goog.provide('my.namespace');" + String.format("%n")
        + "goog.provide('far.out');", sourceFile, Charsets.UTF_8);
    Collection<String> expectedNamespaces = ImmutableSet.of("my.namespace", "far.out");
    Collection<String> actualNamespaces = SourceFileFactory.newJsClosureSourceFile(sourceFile).getProvides();
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test public void inputRequiresOneNamespace() throws IOException {
    Files.write("goog.require('my.namespace');", sourceFile, Charsets.UTF_8);
    Collection<String> expectedNamespaces = ImmutableSet.of("my.namespace");
    Collection<String> actualNamespaces = SourceFileFactory.newJsClosureSourceFile(sourceFile).getRequires();
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test public void inputRequiresTwoNamespaces() throws IOException {
    Files.write("goog.require('my.namespace');" + String.format("%n")
        + "goog.require('far.out');", sourceFile, Charsets.UTF_8);
    Collection<String> expectedNamespaces = ImmutableSet.of("my.namespace", "far.out");
    Collection<String> actualNamespaces = SourceFileFactory.newJsClosureSourceFile(sourceFile).getRequires();
    assertEquals(expectedNamespaces, actualNamespaces);
  }

  @Test public void inputIsValidBaseJs() throws IOException {
    Files.write("var goog = goog || {}; // Identifies this file "
        + "as the Closure base.", baseJs, Charsets.UTF_8);
    JsClosureSourceFile input = SourceFileFactory.newJsClosureSourceFile(baseJs);
    Collection<String> expectedNamespaces = ImmutableSet.of("goog");
    Collection<String> actualNamespaces = input.getProvides();
    assertEquals(expectedNamespaces, actualNamespaces);
    assertTrue(input.isBaseJs());
  }

  @Test(expected = IllegalStateException.class) public void
  inputIsValidBaseJsButProvidesAdditionalNamespace() throws IOException {
    Files.write("var goog = goog || {}; // Identifies this file as the "
        + "Closure base." + String.format("%n")
        + "goog.provide('illegal.namespace.in.basejs');",
        baseJs, Charsets.UTF_8);
    SourceFileFactory.newJsClosureSourceFile(baseJs);
  }

  @Test(expected = IllegalStateException.class) public void
  inputIsValidBaseJsButRequiresAdditionalNamespace() throws IOException {
    Files.write("var goog = goog || {}; // Identifies this file as the "
        + "Closure base." + String.format("%n")
        + "goog.require('illegal.namespace.in.basejs');", baseJs,
        Charsets.UTF_8);
    SourceFileFactory.newJsClosureSourceFile(baseJs);
  }

  @Test public void inputIsBaseJsButWrongFileName() throws IOException {
    Files.write("var goog = goog || {}; // Identifies this file "
        + "as the Closure base.", sourceFile, Charsets.UTF_8);
    assertFalse(SourceFileFactory.newJsClosureSourceFile(sourceFile).isBaseJs());
  }

  @Test public void inputIsBaseJsButWrongContent() throws IOException {
    Files.write("var goog = 42;", baseJs, Charsets.UTF_8);
    assertFalse(SourceFileFactory.newJsClosureSourceFile(baseJs).isBaseJs());
  }
}