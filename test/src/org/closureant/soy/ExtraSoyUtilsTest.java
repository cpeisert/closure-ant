/*
 * Copyright 2009 Google Inc.
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

package org.closureant.soy;

import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;

import com.google.template.soy.data.restricted.PrimitiveData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ExtraSoyUtils}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class ExtraSoyUtilsTest {

  private static final String jsonNestedArray =
      "{'name': 'Ana', 'additionalNames': ['Bob', 'Cid', 'Dee']}";
  private static final String jsonNestedOjbect =
      "{'desk': {'drawers': 5, 'material': 'wood'}}";
  private static final String jsonSimpleObject =
      "{'name': 'Ana', 'age': 43, 'car': null, 'single': false}";

  @Test public void parseJsonToSoyMapData_simpleObject() {
    SoyMapData mapData = ExtraSoyUtils.parseJsonToSoyMapData(jsonSimpleObject);
    SoyMapData expected = new SoyMapData(
        "name", "Ana",
        "age", 43,
        "car", null,
        "single", Boolean.FALSE);
    assertEquals(expected.toString(), mapData.toString());
  }

  @Test public void parseJsonToSoyMapData_nestedArray() {
    SoyMapData mapData = ExtraSoyUtils.parseJsonToSoyMapData(jsonNestedArray);
    SoyMapData expected = new SoyMapData("name", "Ana",
        "additionalNames", new SoyListData("Bob", "Cid", "Dee"));
    assertEquals(expected.toString(), mapData.toString());
  }

  @Test public void parseJsonToSoyMapData_nestedObject() {
    SoyMapData mapData = ExtraSoyUtils.parseJsonToSoyMapData(jsonNestedOjbect);
    SoyMapData expected = new SoyMapData("desk", new SoyMapData("drawers", 5,
        "material", "wood"));
    assertEquals(expected.toString(), mapData.toString());
  }

  @Test public void parseStringToSoyPrimitiveData() {
    PrimitiveData nullData = ExtraSoyUtils.parseStringToPrimitiveData("null");
    assertEquals("null", nullData.toString());

    PrimitiveData booleanData = ExtraSoyUtils.parseStringToPrimitiveData("true");
    assertEquals(true, booleanData.booleanValue());

    PrimitiveData intData = ExtraSoyUtils.parseStringToPrimitiveData("42");
    assertEquals(42, intData.integerValue());

    PrimitiveData floatData = ExtraSoyUtils.parseStringToPrimitiveData("0.25");
    assertEquals(0.25, floatData.floatValue(), 0.0001);

    PrimitiveData stringData = ExtraSoyUtils.parseStringToPrimitiveData("'hi!'");
    assertEquals("hi!", stringData.stringValue());
  }
}