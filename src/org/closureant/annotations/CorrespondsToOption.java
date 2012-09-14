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

package org.closureant.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The presence of this annotation on a method defining an Ant attribute (for
 * example, {@code setMyAttribute(String)}) or an Ant nested element (for
 * example, {@code addMyNestedElement(NestedElementType)}) indicates that the
 * attribute or element corresponds to the option named by the annotation
 * value. For example, the Ant task "Javac" has an attribute {@code destdir}
 * that corresponds to the javac command line option {@code -d}. The
 * definition of the {@code destdir} attribute could be annotated as follows:
 *
 * <p><pre>{@literal @CorrespondsToOption("-d")
public void setDestdir(File destDir) {
  this.destDir = destDir;
}
 * }</pre></p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CorrespondsToOption {

  /**
   * Name of the application to which this annotated Ant task corresponds.
   */
  String value();
}
