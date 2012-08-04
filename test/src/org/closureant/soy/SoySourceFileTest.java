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

package org.closureant.soy;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.template.soy.jssrc.SoyJsSrcOptions;

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
 * Tests for {@link org.closureextensions.common.JsClosureSourceFile}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class SoySourceFileTest {

  private static SoyJsSrcOptions jsSrcOptions;
  private static File soyFile;

  @BeforeClass
  public static void suiteSetup() {
    jsSrcOptions = new SoyJsSrcOptions();
    jsSrcOptions.setCodeStyle(SoyJsSrcOptions.CodeStyle.CONCAT);

    soyFile = new File("./testFile.soy");
    try {
      soyFile.createNewFile();

      String soyContents = new StringBuilder()
          .append("{namespace test}").append(String.format("%n"))
          .append("/** Obligatory template doc comment. */")
          .append(String.format("%n"))
          .append("{template .disclaimer}").append(String.format("%n"))
          .append("<p>By swallowing either the red pill or the blue pill I ")
          .append("agree to forever hold Morpheus harmless for all purposes.")
          .append("</p>").append(String.format("%n"))
          .append("{/template}").append(String.format("%n")).toString();

      Files.write(soyContents, soyFile, Charsets.UTF_8);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @AfterClass
  public static void suiteTearDown() {
    soyFile.delete();
  }

  @Test public void compileToJsSrc() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyFile).build();
    SoySourceFile soySourceFile = helper.soySourceFileIterator().next();
    String jsSource = soySourceFile.compileToJsSrc();
    assertTrue(jsSource.contains("test.disclaimer"));
  }

  @Test public void compileToJsSrcForEachLocale() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyFile).build();
    SoySourceFile soySourceFile = helper.soySourceFileIterator().next();
    soySourceFile.compileToJsSrcForEachLocale(); // Uses default locale "en".
  }

  @Test public void generateParseInfo() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .javaParseInfo("org.mydomain.test", "filename")
        .sourceFile(soyFile).build();
    SoySourceFile soySourceFile = helper.soySourceFileIterator().next();
    ImmutableMap<String, String> parseInfo = soySourceFile.generateParseInfo();
    String generatedFileName = parseInfo.keySet().iterator().next();
    String generatedFileContent = parseInfo.values().iterator().next();
    assertTrue(generatedFileName.contains("SoyInfo.java"));
    assertTrue(generatedFileContent.contains("DISCLAIMER"));
  }

  @Test public void getCode() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyFile).build();
    SoySourceFile soySourceFile = helper.soySourceFileIterator().next();
    String soyCode = soySourceFile.getCode();
    assertTrue(soyCode.contains("{namespace test}"));
  }

  @Test public void getDirectory() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyFile).build();
    SoySourceFile soySourceFile = helper.soySourceFileIterator().next();
    String directory = soySourceFile.getDirectory();
    assertFalse(directory.contains("testFile.soy"));
  }

  @Test public void getNameNoExtension() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyFile).build();
    SoySourceFile soySourceFile = helper.soySourceFileIterator().next();
    String nameNoExt = soySourceFile.getNameNoExtension();
    assertEquals("testFile", nameNoExt);
  }

  @Test public void getNamespace() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyFile).build();
    SoySourceFile soySourceFile = helper.soySourceFileIterator().next();
    String namespace = soySourceFile.getNamespace();
    assertEquals("test", namespace);
  }

  @Test public void getProvides() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyFile).build();
    SoySourceFile soySourceFile = helper.soySourceFileIterator().next();
    Collection<String> provides = soySourceFile.getProvides();
    String namespace = provides.iterator().next();
    assertEquals("test", namespace);
  }
}