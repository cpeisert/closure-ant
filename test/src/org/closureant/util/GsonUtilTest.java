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

package org.closureant.util;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

/**
 * Tests for {@link GsonUtil}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class GsonUtilTest {

  private static final String jsonNestedArray =
      "{'name': 'Ana', 'additionalNames': ['Bob', 'Cid', 'Dee']}";
  private static final String jsonNestedOjbect =
      "{'desk': {'drawers': 5, 'material': 'wood'}}";
  private static final String jsonSimpleObject =
      "{'name': 'Ana', 'age': 43, 'car': null, 'single': false}";

  @Test
  public void parseJsonToMap_simpleObject() {
    Map<String, Object> mapData = GsonUtil.parseJsonToMap(jsonSimpleObject);
    Map<String, Object> expected = Maps.newHashMap();
    expected.put("name", "Ana");
    expected.put("age", 43);
    expected.put("car", null);
    expected.put("single", Boolean.FALSE);
    assertEquals(expected.toString(), mapData.toString());
  }

  @Test
  public void parseJsonToMap_nestedArray() {
    Map<String, Object> mapData = GsonUtil.parseJsonToMap(jsonNestedArray);
    Map<String, Object> expected = Maps.newHashMap();
    expected.put("name", "Ana");
    List<String> list = Lists.newLinkedList();
    list.add("Bob");
    list.add("Cid");
    list.add("Dee");
    expected.put("additionalNames", list);
    assertEquals(expected.toString(), mapData.toString());
  }

  @Test
  public void parseJsonToMap_nestedObject() {
    Map<String, Object> mapData = GsonUtil.parseJsonToMap(jsonNestedOjbect);
    Map<String, Object> expected = Maps.newHashMap();
    Map<String, Object> nestedMap = Maps.newHashMap();
    nestedMap.put("drawers", 5);
    nestedMap.put("material", "wood");
    expected.put("desk", nestedMap);
    assertEquals(expected.toString(), mapData.toString());
  }
}