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

package org.closureant.ant.types;

import com.google.common.collect.Lists;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.List;

/**
 * An object to store {@link PlovrOutputModule} objects to be serialized to a
 * plovr JSON config files.
 *
 * <p>This object provides a custom Gson serializer to ensure that the
 * serialized modules conform to the plovr config-file specification.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class PlovrOutputModuleCollection {

  private final List<PlovrOutputModule> modules;

  /**
   * Constructs a new object to store modules.
   */
  public PlovrOutputModuleCollection() {
    this.modules = Lists.newArrayList();
  }

  /**
   * @return {@code true} if there are no defines options
   */
  public boolean isEmpty() {
    return modules.isEmpty();
  }

  /**
   * Add a module.
   *
   * @param module a Closure JavaScript module
   */
  public void add(PlovrOutputModule module) {
    modules.add(module);
  }

  /**
   * @return the list of modules
   */
  public List<PlovrOutputModule> getModules() {
    return modules;
  }

  /**
   * Custom Gson serializer to ensure that the JSON representation of the
   * modules conforms to the plovr config-file specification.
   */
  public class Serializer
      implements JsonSerializer<PlovrOutputModuleCollection> {
    public JsonElement serialize(PlovrOutputModuleCollection src,
                                 Type typeOfSrc,
                                 JsonSerializationContext context) {
      JsonObject mapOfModules = new JsonObject();
      
      for (PlovrOutputModule module : modules) {
        JsonObject jsonModule = new JsonObject();

        jsonModule.add("inputs", context.serialize(module.getInputs()));
        jsonModule.add("deps", context.serialize(module.getDeps()));
        
        mapOfModules.add(module.getName(), jsonModule);
      }
      
      return mapOfModules;
    }
  }
}