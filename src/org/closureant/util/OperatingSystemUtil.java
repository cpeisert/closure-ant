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

/**
 * // TODO(cpeisert): delete this file if it isn't used
 *
 * Utility class for operating system related functions.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class OperatingSystemUtil {
  private OperatingSystemUtil() {}

  private final static String operatingSystemName =
      System.getProperty("os.name").toLowerCase();

  public static boolean isMac() {
    return operatingSystemName.contains("mac");
  }

  public static boolean isSolaris() {
    return operatingSystemName.contains("sunos");
  }

  public static boolean isUnix() {
    return (operatingSystemName.contains("nux")
        || operatingSystemName.contains("nix"));
  }

  public static boolean isWindows() {
    return operatingSystemName.contains("win");
  }


}