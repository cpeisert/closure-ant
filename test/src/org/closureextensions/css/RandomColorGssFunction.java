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

package org.closureextensions.css;

import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.CssHexColorNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;

import java.awt.Color;
import java.util.List;
import java.util.Random;

/**
 * Custom GSS function to generate a random color. The function takes a string
 * argument that must be either "light" or "dark". In terms of the HSV color
 * model, the options will return random colors within the following
 * constraints:
 *
 * <p><ul>
 * <li><b>dark</b> - Hue: any color; Saturation: 1; Value: in the range
 * [0.05, 0.25]</li>
 * <li><b>light</b> - Hue: any color; Saturation: in the range
 * [0.05, 0.20]; Value: 1</li>
 * </ul></p>
 *
 * @see CustomGssFunctionMapProvider
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class RandomColorGssFunction implements GssFunction {

  private static final Random generator = new Random();

  @Override
  public Integer getNumExpectedArguments() {
    return 1;
  }

  @Override
  public List<CssValueNode> getCallResultNodes(List<CssValueNode> args,
      ErrorManager errorManager) throws GssFunctionException {
    CssValueNode arg = args.get(0);
    String randomColor = null;

    if ("light".equalsIgnoreCase(arg.getValue())) {
      randomColor = generateLightRandomColor();
    } else if ("dark".equalsIgnoreCase(arg.getValue())) {
      randomColor = generateDarkRandomColor();
    } else {
      String message = "The argument must be \"light\" or \"dark\" but was \""
          + arg.getValue() + "\"";
      errorManager.report(new GssError(message, arg.getSourceCodeLocation()));
      throw new GssFunctionException(message);
    }

    CssHexColorNode hexColor = new CssHexColorNode(randomColor,
        arg.getSourceCodeLocation());
    return ImmutableList.of((CssValueNode)hexColor);
  }

  @Override
  public String getCallResultString(List<String> args)
      throws GssFunctionException {
    String arg = args.get(0);
    String randomColor = null;

    if ("light".equalsIgnoreCase(arg)) {
      randomColor = generateLightRandomColor();
    } else if ("dark".equalsIgnoreCase(arg)) {
      randomColor = generateDarkRandomColor();
    } else {
      String message = "The argument must be \"light\" or \"dark\" but was \""
          + arg + "\"";
      throw new GssFunctionException(message);
    }

    return randomColor;
  }

  /**
   * @return a random, dark-shaded color
   * @see RandomColorGssFunction
   */
  private String generateDarkRandomColor() {
    Float hue = generator.nextFloat();
    Float saturation = 1.0f;
    Float value = (generator.nextFloat() * 0.15f) + 0.05f;

    return formatColor(Color.getHSBColor(hue, saturation, value));
  }

  /**
   * @return a random, light-shaded color
   * @see RandomColorGssFunction
   */
  private String generateLightRandomColor() {
    Float hue = generator.nextFloat();
    Float saturation = (generator.nextFloat() * 0.20f) + 0.05f;
    Float value = 1.0f;

    return formatColor(Color.getHSBColor(hue, saturation, value));
  }


  private String formatColor(Color color) {
    return String.format("#%02X%02X%02X",
        color.getRed(), color.getGreen(), color.getBlue());
  }
}