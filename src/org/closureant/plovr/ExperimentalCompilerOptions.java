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

package org.closureant.plovr;

import com.google.common.collect.Maps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;

/**
 * Object representing the plovr config option
 * {@code experiment-compiler-options}.
 *
 * <p>This object also provides a custom Gson serializer for creating plovr
 * config files to ensure that {@link com.google.gson.JsonPrimitive} is
 * correctly serialized to the corresponding primitive JavaScript type rather
 * than a JavaScript object with a {@code value} field.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ExperimentalCompilerOptions {
  private final Map<String, JsonPrimitive> experimentalCompilerOptions;

  /**
   * Constructs a new object to store experimental Compiler options.
   */
  public ExperimentalCompilerOptions() {
    this.experimentalCompilerOptions = Maps.newHashMap();
  }

  /**
   * Exposes {@link java.util.Map#entrySet()} for the underlying map storing
   * the experimental Compiler options.
   *
   * @return a map entry set
   */
  public Set<Map.Entry<String, JsonPrimitive>> entrySet() {
    return experimentalCompilerOptions.entrySet();
  }

  /**
   * @return {@code true} if there are no experimental compiler options
   */
  public boolean isEmpty() {
    return experimentalCompilerOptions.isEmpty();
  }

  /**
   * Adds an experimental Compiler option.
   *
   * @param key the name of the Compiler option
   * @param value the value to assign to the Compiler option
   * @return the previous Compiler option value for {@code key} or
   *     {@code null} if the {@code key} was not set
   */
  public JsonPrimitive put(String key, JsonPrimitive value) {
    return experimentalCompilerOptions.put(key, value);
  }

  /**
   * Add an experimental Compiler option.
   *
   * @param compilerOption the Compiler option to add; the {@code name},
   *     {@code value}, and {@code type} fields must all be set. {@code Type}
   *     must be one of "boolean", "number", or "string".
   * @return the previous Compiler option value for {@code name} or
   *     {@code null} if the {@code name} was not set
   */
  public JsonPrimitive put(Parameter compilerOption) {
    JsonPrimitive previousValue;

    if ("boolean".equalsIgnoreCase(compilerOption.getType())) {
      previousValue = experimentalCompilerOptions.put(compilerOption.getName(),
          new JsonPrimitive(Boolean.parseBoolean(compilerOption.getValue())));
    } else if ("number".equalsIgnoreCase(compilerOption.getType())) {
      previousValue = experimentalCompilerOptions.put(compilerOption.getName(),
          new JsonPrimitive(new BigDecimal(compilerOption.getValue())));
    } else if ("string".equalsIgnoreCase(compilerOption.getType())) {
      previousValue = experimentalCompilerOptions.put(compilerOption.getName(),
          new JsonPrimitive(compilerOption.getValue()));
    } else {
      throw new BuildException("<experimentalCompilerOption> \"type\" "
          + "attribute expected to be one of \"boolean\", \"number\", or "
          + "or \"string\" but was \"" + compilerOption.getType() + "\"");
    }
    return previousValue;
  }

  /**
   * Custom Gson serializer to ensure that {@link JsonPrimitive} is correctly
   * serialized to the corresponding primitive JavaScript type rather than a
   * JavaScript object with a {@code value} field.
   */
  public class Serializer
      implements JsonSerializer<ExperimentalCompilerOptions> {
    public JsonElement serialize(ExperimentalCompilerOptions src,
                                 Type typeOfSrc,
                                 JsonSerializationContext context) {
      JsonObject map = new JsonObject();
      for (Map.Entry<String, JsonPrimitive> compilerOption :
          experimentalCompilerOptions.entrySet()) {
        if (compilerOption.getValue().isBoolean()) {
          map.addProperty(compilerOption.getKey(),
              compilerOption.getValue().getAsBoolean());
        } else if (compilerOption.getValue().isNumber()) {
          map.addProperty(compilerOption.getKey(),
              compilerOption.getValue().getAsNumber());
        } else if (compilerOption.getValue().isString()) {
          map.addProperty(compilerOption.getKey(),
              compilerOption.getValue().getAsString());
        }
      }
      return map;
    }
  }
}