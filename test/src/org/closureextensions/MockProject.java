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

package org.closureextensions;

import com.google.common.base.Throwables;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Static utility class providing a mock Ant project for unit tests.
 *
 * @author cpeisert@gmail.com (Christopher Peisert)
 */
public final class MockProject {
  private MockProject() {}

  private static final Project project;

  static {
    project = new Project();
    project.setName("test-project");
    project.init();
    project.addBuildListener(new AntTestListener(Project.MSG_DEBUG));
  }

  public static Project getProject() {
    return project;
  }

  /**
   * @return the full contents of the Ant log (all logging levels)
   */
  public static String getFullAntLog() {
    AntTestListener listener = (AntTestListener)getProject()
        .getBuildListeners().firstElement();
    return listener.getFullLog();
  }

  /**
   * @return the Ant log including levels info, warnings, and errors
   */
  public static String getAntLog() {
    AntTestListener listener = (AntTestListener)getProject()
        .getBuildListeners().firstElement();
    return listener.getLog();
  }

  /**
   * clear the Ant logs
   */
  public static void clearAntLogs() {
    AntTestListener listener = (AntTestListener)getProject()
        .getBuildListeners().firstElement();
    listener.clearLogs();
  }

  /**
   * @param property name of the property to set
   * @param value the value for the property
   */
  public static void setProperty(String property, String value) {
    project.setProperty(property, value);
  }
  
  /**
   * Unset an Ant project property using reflection to reach inside the
   * project. Adapted from the ant-contrib project.
   * See <a href="http://ant-contrib.svn.sourceforge.net/viewvc/ant-contrib/ant-contrib/trunk/src/main/java/net/sf/antcontrib/property/Variable.java?revision=177&view=markup">
   * Variable.java</a>
   *
   * Note: This implementation requires Ant 1.6 or newer since previous
   * versions of Ant do not use a {@link org.apache.tools.ant.PropertyHelper}.
   *
   * @param propertyName name of the property to unset
   * @throws IllegalAccessException foiled by the security manager
   * @throws NoSuchFieldException if "properties" field not found inside the
   *     project's {@code PropertyHelper} or if there was no property with name
   *     {@code propertyName}
   */
  public static void unsetProperty(String propertyName) {
    try {
      PropertyHelper propertyHelper = (PropertyHelper)project
          .getReference("ant.PropertyHelper");
      if (propertyHelper != null) {
        Field field = getField(propertyHelper.getClass(), "properties");
        field.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String, String> properties =
            (Map<String, String>)field.get(propertyHelper);
        if (properties != null) {
          String value = properties.remove(propertyName);
          if (value == null) {
            throw new NoSuchFieldException("Ant property \"" + propertyName
                + "\" was not defined in project \"" + project.getName() + '"');
          }
        } else {
          throw new NoSuchFieldException("the Ant project \"" + project.getName()
              + "\" did not contain a \"properties\" field from which to unset "
              + "property \"" + propertyName + '"');
        }
      }
    } catch (Throwable throwable) {
      Throwables.propagateIfPossible(throwable);
      throw new RuntimeException("unexpected", throwable);
    }
  }

  private static Field getField(Class<?> thisClass, String fieldName)
      throws NoSuchFieldException {

    if (thisClass == null) {
      throw new NoSuchFieldException("Invalid field: \"" + fieldName + '"');
    }
    try {
      return thisClass.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      return getField(thisClass.getSuperclass(), fieldName);
    }
  }
}