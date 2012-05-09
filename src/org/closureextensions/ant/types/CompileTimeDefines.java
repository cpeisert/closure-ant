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

package org.closureextensions.ant.types;

import com.google.common.collect.Maps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Object representing a collection of compile-time defines. The equivalent 
 * Closure Compiler command line option is {@code --define}. 
 * 
 * <p>This object also provides a custom Gson serializer for creating plovr 
 * config files to ensure that {@link com.google.gson.JsonPrimitive} is 
 * correctly serialized to the corresponding primitive JavaScript type rather
 * than a JavaScript object with a {@code value} field.</p>
 *
 * <p>For more information about compile-time defines, see: <a target="_blank"
 * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
 * Definitive Guide</i></a> page 353.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class CompileTimeDefines {
  private final Map<String, JsonPrimitive> defines;

  /**
   * Constructs a new object to store compile-time defines.
   */
  public CompileTimeDefines() {
    this.defines = Maps.newHashMap();
  }

  /**
   * Exposes {@link java.util.Map#entrySet()} for the underlying map storing
   * the defines options.
   *
   * @return a map entry set
   */
  public Set<Map.Entry<String, JsonPrimitive>> entrySet() {
    return defines.entrySet();
  }

  /**
   * @return {@code true} if there are no defines options
   */
  public boolean isEmpty() {
    return defines.isEmpty();
  }

  /**
   * Add a compile-time define.
   *
   * @param key the name of the JavaScript variable to define at compile time
   * @param value the value to assign to the JavaScript variable
   * @return the previous value defined for {@code key} or {@code null} if
   *     the {@code key} was not set
   */
  public JsonPrimitive put(String key, JsonPrimitive value) {
    return defines.put(key, value);
  }

  /**
   * Add a compile-time define.
   *
   * @param define a compile-time define; the {@code name},
   *     {@code value}, and {@code type} fields must all be set. {@code Type}
   *     must be one of "boolean", "number", or "string".
   * @return the previous value defined for {@code name} or {@code null} if
   *     the {@code name} was not set
   */
  public JsonPrimitive put(Parameter define) {
    JsonPrimitive previousValue;

    if ("boolean".equalsIgnoreCase(define.getType())) {
      previousValue = defines.put(define.getName(),
          new JsonPrimitive(Boolean.parseBoolean(define.getValue())));
    } else if ("number".equalsIgnoreCase(define.getType())) {
      previousValue = defines.put(define.getName(),
          new JsonPrimitive(new BigDecimal(define.getValue())));
    } else if ("string".equalsIgnoreCase(define.getType())) {
      previousValue = defines.put(define.getName(),
          new JsonPrimitive(define.getValue()));
    } else {
      throw new BuildException("<define> \"type\" "
          + "attribute expected to be one of \"boolean\", \"number\", or "
          + "or \"string\" but was \"" + define.getType() + "\"");
    }
    return previousValue;
  }

  /**
   * Custom Gson serializer to ensure that {@link JsonPrimitive} is correctly
   * serialized to the corresponding primitive JavaScript type rather than a
   * JavaScript object with a {@code value} field.
   */
  public class Serializer
      implements JsonSerializer<CompileTimeDefines> {
    public JsonElement serialize(CompileTimeDefines src,
                                 Type typeOfSrc,
                                 JsonSerializationContext context) {
      JsonObject map = new JsonObject();
      for (Map.Entry<String, JsonPrimitive> define : defines.entrySet()) {
        if (define.getValue().isBoolean()) {
          map.addProperty(define.getKey(),
              define.getValue().getAsBoolean());
        } else if (define.getValue().isNumber()) {
          map.addProperty(define.getKey(),
              define.getValue().getAsNumber());
        } else if (define.getValue().isString()) {
          map.addProperty(define.getKey(),
              define.getValue().getAsString());
        }
      }
      return map;
    }
  }
}