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
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.jssrc.SoyJsSrcOptions;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.xliffmsgplugin.XliffMsgPluginModule;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

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
public final class SoyHelperTest {

  private static SoyJsSrcOptions jsSrcOptions;
  private static File soyTestFile;
  private static File soyTestUtilFile;
  private static File xliffGerman;

  @BeforeClass
  public static void suiteSetup() {
    jsSrcOptions = new SoyJsSrcOptions();
    jsSrcOptions.setCodeStyle(SoyJsSrcOptions.CodeStyle.CONCAT);
    jsSrcOptions.setShouldProvideRequireSoyNamespaces(true);

    soyTestFile = new File("./test.soy");
    soyTestUtilFile = new File("./test_util.soy");
    xliffGerman = new File("./translations_de.xlf");
    try {
      soyTestFile.createNewFile();
      String soyTestContent = new StringBuilder()
          .append("{namespace test}").append(String.format("%n"))
          .append("/** @param indemnifiedParty */")
          .append(String.format("%n"))
          .append("{template .disclaimer}").append(String.format("%n"))
          .append("{msg desc=\"Legal disclaimer\"}").append(String.format("%n"))
          .append("By swallowing either the red pill or the blue pill I ")
          .append("agree to forever hold {$indemnifiedParty} harmless for ")
          .append("all purposes.{/msg}").append(String.format("%n"))
          .append("{call test.util.printConfirmation /}")
          .append(String.format("%n"))
          .append("{/template}").append(String.format("%n")).toString();
      Files.write(soyTestContent, soyTestFile, Charsets.UTF_8);

      soyTestUtilFile.createNewFile();
      String soyTestUtilContent = new StringBuilder()
          .append("{namespace test.util}").append(String.format("%n"))
          .append("/** Print confirmation message. */")
          .append(String.format("%n"))
          .append("{template .printConfirmation}").append(String.format("%n"))
          .append("{msg desc=\"Confirmation message for pill color.\"}")
          .append(String.format("%n"))
          .append("Please confirm by clicking RED or BLUE.{/msg}")
          .append(String.format("%n"))
          .append("{/template}").append(String.format("%n")).toString();
      Files.write(soyTestUtilContent, soyTestUtilFile, Charsets.UTF_8);

      xliffGerman.createNewFile();
      String xliffTranslations = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          + String.format("%n")
          + "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:12\">"
          + String.format("%n")
          + "<file original=\"SoyMsgBundle\" datatype=\"x-soy-msg-bundle\" xml:space=\"preserve\" source-language=\"en\" target-language=\"de\">"
          + String.format("%n")
          + "<body>" + String.format("%n")
          + "<trans-unit id=\"2023358506807474016\" datatype=\"html\">"
          + String.format("%n")
          + "<source>Please confirm by clicking RED or BLUE.</source>"
          + String.format("%n")
          + "<target>Bitte bestätigen Sie durch Klicken auf ROT oder BLAU.</target>"
          + String.format("%n")
          + "<note priority=\"1\" from=\"description\">Confirmation message for pill color.</note>"
          + String.format("%n")
          + "</trans-unit>" + String.format("%n")
          + "<trans-unit id=\"3303999997970681015\" datatype=\"html\">"
          + String.format("%n")
          + "<source>By swallowing either the red pill or the blue pill I agree to forever hold <x id=\"INDEMNIFIED_PARTY\"/> harmless for all purposes.</source>"
          + String.format("%n")
          + "<target>Durch Schlucken entweder die rote Pille oder die blaue Pille Ich stimme für immer, das Ich <x id=\"INDEMNIFIED_PARTY\"/> unschädlich für alle Zwecke halten werde.</target>"
          + String.format("%n")
          + "<note priority=\"1\" from=\"description\">Legal disclaimer</note>"
          + String.format("%n")
          + "</trans-unit>" + String.format("%n")
          + "</body>" + String.format("%n")
          + "</file>" + String.format("%n")
          + "</xliff>" + String.format("%n");

      Files.write(xliffTranslations, xliffGerman, Charsets.UTF_8);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @AfterClass
  public static void suiteTearDown() {
    soyTestFile.delete();
    soyTestUtilFile.delete();
    xliffGerman.delete();
  }

  @Test public void extractMessagesToSoyMsgBundle() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyTestFile).sourceFile(soyTestUtilFile).build();
    SoyMsgBundle bundleOfLinguisticJoy = helper.extractMessagesToSoyMsgBundle();
    assertEquals(2, bundleOfLinguisticJoy.getNumMsgs());
  }

  @Test public void extractMessagesToFile() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyTestFile).sourceFile(soyTestUtilFile)
        .extractedMessageSourceLocale("en").extractedMessagesTargetLocale("de")
        .build();
    String xliff = helper.extractMessagesForEachTargetLocale().get("de");
    assertTrue(xliff.contains("en"));
    assertTrue(xliff.contains("de"));
    assertTrue(xliff.contains("Legal disclaimer"));
    assertTrue(xliff.contains("Confirmation message"));
  }

  @Test public void generateParseInfo() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyTestFile).sourceFile(soyTestUtilFile)
        .javaParseInfo("org.mydomain.test", "filename").build();
    ImmutableMap<String, String> parseInfo = helper.generateParseInfo();
    assertEquals(2, parseInfo.entrySet().size());
  }

  @Test public void nullClassLoader() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyTestFile).sourceFile(soyTestUtilFile)
        .classLoader(null)
        .build();
  }

  @Test public void soyCustomFunctionPlugin() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyTestFile).sourceFile(soyTestUtilFile)
        .pluginModule("org.plovr.soy.function.PlovrModule")
        .build();
  }

  @Test public void soySourceFileIterator() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyTestFile).sourceFile(soyTestUtilFile).build();
    Iterator<SoySourceFile> i = helper.soySourceFileIterator();

    assertTrue(i.hasNext());
    i.next();
    assertTrue(i.hasNext());
    i.next();
    assertFalse(i.hasNext());
  }

  @Test public void render() throws IOException {
    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyTestFile).sourceFile(soyTestUtilFile).build();
    SoyMapData params = new SoyMapData("indemnifiedParty", "Morpheus");
    String renderedContent = helper.render("test.disclaimer", params, null);
    assertTrue(renderedContent.contains("forever hold Morpheus harmless"));
    assertTrue(renderedContent.contains("confirm by clicking RED or BLUE"));
  }

  @Test public void renderForEachLocale() {
    SoyHelper helper = null;
    try {
      helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
          .sourceFile(soyTestFile).sourceFile(soyTestUtilFile)
          .parseTranslationsFromXliffFile(xliffGerman).build();
    } catch (IOException e) {
      e.printStackTrace();
    }
    SoyMapData params = new SoyMapData("indemnifiedParty", "Morpheus");
    ImmutableMap<String, String> localeToRendering =
        helper.renderForEachLocale("test.disclaimer", params, null);
    assertEquals(1, localeToRendering.entrySet().size());
    assertEquals("de", localeToRendering.keySet().iterator().next());

    String renderedGerman = localeToRendering.values().iterator().next();
    assertTrue(renderedGerman.contains("Morpheus unschädlich für alle Zwecke"));
  }

  @Test public void renderForEachLocale_explicitMessagePluginModule()
      throws IOException {

    SoyHelper helper = new SoyHelper.Builder().soyJsSrcOptions(jsSrcOptions)
        .sourceFile(soyTestFile).sourceFile(soyTestUtilFile)
        .parseTranslationsFromFile(xliffGerman)
        .messagePluginModule(XliffMsgPluginModule.class.getName())
        .build();
    SoyMapData params = new SoyMapData("indemnifiedParty", "Morpheus");
    ImmutableMap<String, String> localeToRendering =
        helper.renderForEachLocale("test.disclaimer", params, null);
    assertEquals(1, localeToRendering.entrySet().size());
    assertEquals("de", localeToRendering.keySet().iterator().next());

    String renderedGerman = localeToRendering.values().iterator().next();
    assertTrue(renderedGerman.contains("Morpheus unschädlich für alle Zwecke"));
  }
}