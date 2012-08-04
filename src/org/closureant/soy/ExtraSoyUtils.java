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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.template.soy.SoyModule;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.internalutils.DataUtils;
import com.google.template.soy.data.restricted.PrimitiveData;
import com.google.template.soy.exprparse.ExpressionParser;
import com.google.template.soy.exprparse.ParseException;
import com.google.template.soy.exprparse.TokenMgrError;
import com.google.template.soy.exprtree.DataRefNode;
import com.google.template.soy.exprtree.ExprNode;
import com.google.template.soy.exprtree.GlobalNode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Soy (Closure Templates) utility class.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ExtraSoyUtils {
  private ExtraSoyUtils() {}

  /**
   * Creates a Guice injector that includes the SoyModule, a message plugin
   * module, and possibly additional plugin modules.
   *
   * @param msgPluginModuleName The full class name of the message plugin
   *     module. Required.
   * @param pluginModuleNames list of the full class names of additional
   *     plugin modules to include. Optional (may be {@code null}).
   * @param classLoader the class loader to use. Optional (may be {@code
   *     null}.
   * @return A Guice injector that includes the SoyModule, the given message
   *     plugin module, and the given additional plugin modules (if any).
   * @throws NullPointerException if {@code msgPluginModuleName} is null
   */
  public static Injector createInjector(String msgPluginModuleName,
      @Nullable List<String> pluginModuleNames,
      @Nullable ClassLoader classLoader) {
    Preconditions.checkNotNull(msgPluginModuleName, "msgPluginModuleName is "
        + "null");

    List<Module> guiceModules = Lists.newArrayListWithCapacity(2);
    guiceModules.add(new SoyModule());
    guiceModules.add(instantiatePluginModule(msgPluginModuleName, classLoader));

    if (pluginModuleNames != null && !pluginModuleNames.isEmpty()) {
      for (String pluginModuleName : pluginModuleNames) {
        guiceModules.add(
            instantiatePluginModule(pluginModuleName, classLoader));
      }
    }

    return Guice.createInjector(guiceModules);
  }


  /**
   * Private helper for createInjector().
   *
   * @param moduleName the name of the plugin module to instantiate
   * @param classLoader the class loader; may be {@code null}
   * @return a new instance of the specified plugin module
   */
  private static Module instantiatePluginModule(String moduleName,
      @Nullable ClassLoader classLoader) {

    try {
      if (classLoader == null) {
        return (Module) Class.forName(moduleName).newInstance();
      } else {
        return (Module)
            Class.forName(moduleName, true, classLoader).newInstance();
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot find plugin module \"" + moduleName
          + "\".", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Cannot access plugin module \"" + moduleName
          + "\".", e);
    } catch (InstantiationException e) {
      throw new RuntimeException("Cannot instantiate plugin module \""
          + moduleName + "\".", e);
    }
  }

  /**
   * Parse JSON to {@link SoyMapData}.
   *
   * @param json JSON string to parse
   * @return a {@link SoyMapData}
   */
  public static SoyMapData parseJsonToSoyMapData(String json) {
    JsonElement element = new JsonParser().parse(json);
    JsonObject tree = element.getAsJsonObject();

    return internalCreateSoyMapDataFromJsonObject(tree);
  }

  /**
   * Construct a {@link SoyMapData} object from a {@link JsonObject}.
   *
   * @param object the {@link JsonObject} to parse
   * @return the {@link SoyMapData}
   */
  private static SoyMapData internalCreateSoyMapDataFromJsonObject(
      JsonObject object) {
    SoyMapData map = new SoyMapData();

    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      String key = entry.getKey();
      JsonElement value = entry.getValue();
      map.put(key, internalGetValueFromJsonElement(value));
    }
    return map;
  }

  /**
   * Parse a JSON element and return the equivalent Soy data structure.
   *
   * @param element the JSON element
   * @return the equivalent Soy data structure
   */
  private static Object internalGetValueFromJsonElement(JsonElement element) {
    if (element.isJsonObject()) {
      return internalCreateSoyMapDataFromJsonObject(element.getAsJsonObject());
    }
    else if (element.isJsonArray()) {
      JsonArray array = element.getAsJsonArray();
      SoyListData list = new SoyListData();
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
      if (primitive.isBoolean()) return primitive.getAsBoolean();
      if (primitive.isString()) return primitive.getAsString();
      // else p is number, but don't know what kind
      String s = primitive.getAsString();
      if (s.matches(".*\\d\\.\\d.*") || s.contains("e")) {
        return new Double(s);
      } else {
        return new Integer(s);
      }
    }
  }

  // The follow code originated from com.google.template.soy.SoyUtils.

  /**
   * Error types for bad lines in the compile-time globals file.
   */
  private static enum CompileTimeGlobalValueError {

    INVALID_VALUE("Invalid Soy primitive value. Make sure strings are single "
        + "quoted."),
    NON_PRIMITIVE_VALUE("Non-primitive value");

    private final String errorString;

    private CompileTimeGlobalValueError(String errorString) {
      this.errorString = errorString;
    }

    @Override public String toString() {
      return errorString;
    }
  }

  /**
   * Parses a string representing one of the Soy primitive types (null,
   * boolean, integer, float [Java double], or string) to a Soy {@link
   * PrimitiveData} object.
   *
   * <p><b>Note:</b> Soy strings must be single quoted.</p>
   *
   * <p>Example Soy type literals:</p>
   *
   * <p><ul>
   * <li>Soy null: {@code null}</li>
   * <li>Soy boolean: {@code true}</li>
   * <li>Soy integer: {@code 42}</li>
   * <li>Soy float: {@code 0.25}</li>
   * <li>Soy string: {@code 'this is a string'}</li>
   * </ul></p>
   *
   * @param valueText the string value to parse
   * @return {@link PrimitiveData} object for the parsed value
   * @throws SoySyntaxException if the value is not in a valid format
   */
  public static PrimitiveData parseStringToPrimitiveData(String valueText)
      throws SoySyntaxException {
    PrimitiveData data;
    try {
      ExprNode valueExpr =
          (new ExpressionParser(valueText)).parseExpression().getChild(0);
      if (!(valueExpr instanceof ExprNode.PrimitiveNode)) {
        if (valueExpr instanceof GlobalNode
            || valueExpr instanceof DataRefNode) {
          throw new SoySyntaxException(
              CompileTimeGlobalValueError.INVALID_VALUE.toString());
        } else {
          throw new SoySyntaxException(
              CompileTimeGlobalValueError.NON_PRIMITIVE_VALUE.toString());
        }
      }
      data = DataUtils.convertPrimitiveExprToData(
          (ExprNode.PrimitiveNode) valueExpr);
    } catch (TokenMgrError tme) {
      throw new SoySyntaxException(
          CompileTimeGlobalValueError.INVALID_VALUE.toString());
    } catch (ParseException pe) {
      throw new SoySyntaxException(
          CompileTimeGlobalValueError.INVALID_VALUE.toString());
    }

    return data;
  }
}