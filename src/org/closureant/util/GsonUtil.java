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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Map;

/**
 * Utility functions extending <a target="_blank"
 * href="http://code.google.com/p/google-gson/">Google-Gson</a>.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class GsonUtil {
  private GsonUtil() {}

  /**
   * Parse JSON to {@link Map}.
   *
   * @param json JSON string to parse
   * @return a {@link Map}
   * @throws JsonParseException if the specified text is not valid JSON
   */
  public static Map<String, Object> parseJsonToMap(String json)
      throws JsonParseException {
    JsonElement element = new JsonParser().parse(json);
    JsonObject tree = element.getAsJsonObject();

    return internalCreateMapFromJsonObject(tree);
  }

  /**
   * Construct a {@link Map} object from a {@link JsonObject}.
   *
   * @param object the {@link JsonObject} to parse
   * @return a {@link Map}
   */
  private static  Map<String, Object> internalCreateMapFromJsonObject(
      JsonObject object) {
    Map<String, Object> map = Maps.newHashMap();

    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      String key = entry.getKey();
      JsonElement value = entry.getValue();
      map.put(key, internalGetValueFromJsonElement(value));
    }
    return map;
  }

  /**
   * Parse a JSON element and return the equivalent Map, List, or primitive
   * wrapper.
   *
   * @param element the JSON element
   * @return the equivalent Map, List, or primitive wrapper for the JSON
   *     element
   */
  private static Object internalGetValueFromJsonElement(JsonElement element) {
    if (element.isJsonObject()) {
      return internalCreateMapFromJsonObject(element.getAsJsonObject());
    }
    else if (element.isJsonArray()) {
      JsonArray array = element.getAsJsonArray();
      List<Object> list = Lists.newLinkedList();
      for (JsonElement e : array) {
        list.add(internalGetValueFromJsonElement(e));
      }
      return list;
    }
    else if (element.isJsonNull()) {
      return null;
    }
    else { // must be primitive
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isBoolean()) {
        return primitive.getAsBoolean();
      }
      if (primitive.isString()) {
        return primitive.getAsString();
      }
      // else p is number, but don't know what kind
      String s = primitive.getAsString();
      if (s.matches(".*\\d\\.\\d.*") || s.contains("e")) {
        return new Double(s);
      } else {
        return new Integer(s);
      }
    }
  }
}