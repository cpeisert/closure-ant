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

package org.closureant.css;

import com.google.common.collect.BiMap;
import com.google.gson.JsonParseException;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CssRenamingMap}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class CssRenamingMapTest {

  private static final String jsonCssRenamingMap =
      "  {\n"
      + "  'goog': 'a',\n"
      + "\"hsv\": \"b\",\n"
      + " \"palette\":\"c\"   "
      + "}";
  private static final String invalidJson =
      "  {\n"
          + "  'goog'\n"
          + "}";
  private static final String jsonWithNonStringValues =
      "  {\n"
          + "  'goog': false,\n"
          + "\"hsv\": 42,\n"
          + " \"palette\": null   "
          + "}";
  private static final String closureCompiledCssRenamingMap =
      "goog.setCssNameMapping({\n"
          + "  'goog': 'a',\n"
          + "\"hsv\": \"b\",\n"
          + " \"palette\":\"c\"   "
          + "});\n";
  private static final String closureUncompiledCssRenamingMap =
      "CLOSURE_CSS_NAME_MAPPING = {\n"
          + "  'goog': 'a',\n"
          + "\"hsv\": \"b\",\n"
          + " \"palette\":\"c\"   "
          + "};\n";
  private static final String propertiesRenamingMap =
      "goog=a\n hsv=b\n palette =c\n";

  private final static CssRenamingMap expected = CssRenamingMap.create();


  @BeforeClass
  public static void suiteSetup() {
    expected.put("goog", "a");
    expected.put("hsv", "b");
    expected.put("palette", "c");
  }

  @Test
  public void createCssRenamingMapFromJson()
      throws IOException, JsonParseException {
    BiMap<String, String> biMap = CssRenamingMap.internalCreateBiMapFromJson(
        jsonCssRenamingMap);
    CssRenamingMap renamingMap = CssRenamingMap.createFromMap(biMap);

    assertTrue(expected.equals(renamingMap));
  }

  @Test(expected = JsonParseException.class)
  public void createCssRenamingMapFromInvalidJson()
      throws IOException, JsonParseException {
    BiMap<String, String> biMap = CssRenamingMap.internalCreateBiMapFromJson(
        invalidJson);
  }

  @Test(expected = ClassCastException.class)
  public void createCssRenamingMapFromJsonWithNonStringValues()
      throws IOException, JsonParseException {
    BiMap<String, String> biMap = CssRenamingMap.internalCreateBiMapFromJson(
        jsonWithNonStringValues);
  }

  @Test
  public void createCssRenamingMapFromClosureCompiledJson()
      throws IOException, JsonParseException {
    BiMap<String, String> biMap = CssRenamingMap.internalCreateBiMapFromJson(
        closureCompiledCssRenamingMap);
    CssRenamingMap renamingMap = CssRenamingMap.createFromMap(biMap);

    assertTrue(expected.equals(renamingMap));
  }

  @Test
  public void createCssRenamingMapFromClosureUncompiledJson()
      throws IOException, JsonParseException {
    BiMap<String, String> biMap = CssRenamingMap.internalCreateBiMapFromJson(
        closureUncompiledCssRenamingMap);
    CssRenamingMap renamingMap = CssRenamingMap.createFromMap(biMap);

    assertTrue(expected.equals(renamingMap));
  }

  @Test
  public void createCssRenamingMapFromJavaProperties()
      throws IOException {
    BiMap<String, String> biMap = CssRenamingMap
        .internalCreateBiMapFromJavaProperties(propertiesRenamingMap);
    CssRenamingMap renamingMap = CssRenamingMap.createFromMap(biMap);

    assertTrue(expected.equals(renamingMap));
  }
}
