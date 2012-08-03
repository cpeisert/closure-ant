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

import com.google.common.collect.ImmutableMap;
import com.google.common.css.GssFunctionMapProvider;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.gssfunctions.DefaultGssFunctionMapProvider;

import java.util.Map;

/**
 * Custom GSS function map provider for testing
 * {@link org.closureant.ant.ClosureStylesheetsTask}.
 *
 * <p>See <a target="_blank" href="http://code.google.com/p/closure-stylesheets/#Functions">
 * Closure Stylesheets: Functions</a>.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class CustomGssFunctionMapProvider
    implements GssFunctionMapProvider {

  private final DefaultGssFunctionMapProvider defaultFunctionMapProvider =
      new DefaultGssFunctionMapProvider();

  public Map<String, GssFunction> get() {
    return new ImmutableMap.Builder<String, GssFunction>()
        // Default functions.
        .putAll(defaultFunctionMapProvider.get())

        // My custom function for testing the Closure Stylesheets Ant task.
        .put("randomColor", new RandomColorGssFunction())
        .build();
  }

  @Override
  public <F> Map<String, F> get(Class<F> gssFunctionClass) {
    if (GssFunction.class.equals(gssFunctionClass)) {
      @SuppressWarnings("unchecked")
      Map<String, F> map = (Map<String, F>) get();
      return map;
    }
    return null;
  }
}