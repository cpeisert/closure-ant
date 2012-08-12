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

package org.closureant.types;

import com.google.common.collect.Lists;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Parameter;

import java.util.List;

/**
 * // TODO(cpeisert): Delete this file once documentation has been migrated.
 *
 * Abstract Closure Compiler object providing common compiler options for
 * implementations of Ant tasks and data types.
 *
 * <p>The provided compiler options are roughly the intersection of the
 * command line interface implemented by
 * <a href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/CommandLineRunner.java">
 * CommandLineRunner.java</a> and the
 * <a href="http://plovr.com/options.html">plovr options</a>, excluding options
 * related to the location of JavaScript inputs, outputs, and their
 * dependencies such as {@code --js}, {@code --manage_closure_dependencies},
 * and {@code --closure_entry_point}. Functionality for specifying JavaScript
 * inputs and their dependencies should be implemented by Ant tasks that wrap
 * the Closure Compiler. See {@link org.closureant.ClosureCompiler},
 * {@link org.closureant.ClosureBuilderPython}, and
 * {@link org.closureant.Plovr}.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public abstract class AbstractClosureCompiler extends ProjectComponent {

  // Attributes

  protected Boolean customExternsOnly;
  protected Boolean debug;
  protected String languageIn;
  protected String outputWrapper;
  protected Boolean prettyPrint;
  protected Boolean printInputDelimiter;
  protected String propertyMapInputFile;
  protected String propertyMapOutputFile;
  protected String variableMapInputFile;
  protected String variableMapOutputFile;
  protected String warningLevel;


  // Nested elements

  protected final List<NameValuePair> compilerChecks;
  protected final CompileTimeDefines defines;
  protected final List<FileSet> externs;

  /**
   * Constructor for subclasses to initialize fields.
   */
  protected AbstractClosureCompiler() {
    super();

    // Attributes

    this.customExternsOnly = null;
    this.debug = null;
    this.languageIn = "";
    this.outputWrapper = "";
    this.prettyPrint = null;
    this.printInputDelimiter = null;
    this.propertyMapInputFile = "";
    this.propertyMapOutputFile = "";
    this.variableMapInputFile = "";
    this.variableMapOutputFile = "";
    this.warningLevel = "";

    // Nested elements

    this.compilerChecks = Lists.newArrayList();
    this.defines = new CompileTimeDefines();
    this.externs = Lists.newArrayList();
  }


  // Attribute setters

  /**
   * @param customExternsOnly whether only the custom externs specified by
   *     {@code externs} should be used (rather than in addition to the
   *     default externs bundled with the Closure Compiler). Defaults to
   *     {@code false}.
   */
  public void setCustomExternsOnly(boolean customExternsOnly) {
    this.customExternsOnly = customExternsOnly;
  }

  /**
   * @param debug Enable debugging options. Defaults to {@code false}.
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * @param languageIn the language specification that input sources conform.
   *     Options: "ECMASCRIPT3", "ECMASCRIPT5", "ECMASCRIPT5_STRICT". Defaults
   *     to "ECMASCRIPT3".
   * @throws BuildException if {@code languageIn} is not a valid option
   */
  public void setLanguageIn(String languageIn) {
    if ("ECMASCRIPT3".equalsIgnoreCase(languageIn)
        || "ECMASCRIPT5".equalsIgnoreCase(languageIn)
        || "ECMASCRIPT5_STRICT".equalsIgnoreCase(languageIn)) {
      
      this.languageIn = languageIn.toUpperCase();
    } else {
      throw new BuildException("languageIn expected to be one of "
          + " \"ECMASCRIPT3\", \"ECMASCRIPT5\", or \"ECMASCRIPT5_STRICT\", "
          + "but was \"" + languageIn.toUpperCase() + "\"");
    }
  }

  /**
   * @param outputWrapper a template into which compiled JavaScript will be
   *     written; the placeholder for compiled code is {@code %output%}
   */
  public void setOutputWrapper(String outputWrapper) {
    this.outputWrapper = outputWrapper;
  }

  /**
   * @param prettyPrint Equivalent to the command-line
   *     {@code --formatting=PRINT_INPUT_DELIMITER} flag for the Closure
   *     Compiler. Defaults to {@code false}.
   */
  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  /**
   * @param printInputDelimiter Equivalent to the command-line
   *     {@code --formatting=PRETTY_PRINT} flag for the Closure Compiler.
   *     Defaults to false.
   */
  public void setPrintInputDelimiter(boolean printInputDelimiter) {
    this.printInputDelimiter = printInputDelimiter;
  }
  
  /**
   * @param inputFile file containing the serialized version of the property
   *     renaming map produced by a previous compilation
   */
  public void setPropertyMapInputFile(String inputFile) {
    this.propertyMapInputFile = inputFile;
  }

  /**
   * @param outputFile file where the serialized version of the property
   *     renaming map produced should be saved
   */
  public void setPropertyMapOutputFile(String outputFile) {
    this.propertyMapOutputFile = outputFile;
  }

  /**
   * @param inputFile file containing the serialized version of the variable
   *     renaming map produced by a previous compilation
   */
  public void setVariableMapInputFile(String inputFile) {
    this.variableMapInputFile = inputFile;
  }

  /**
   * @param outputFile file where the serialized version of the variable
   *     renaming map produced should be saved
   */
  public void setVariableMapOutputFile(String outputFile) {
    this.variableMapOutputFile = outputFile;
  }

  /**
   * @param warningLevel the warning level, which must be one of "QUIET",
   *     "DEFAULT", or "VERBOSE". Defaults to "DEFAULT".
   * @throws BuildException if {@code warningLevel} is not a valid option
   */
  public void setWarningLevel(String warningLevel) {
    String warningLevelUpperCase = warningLevel.toUpperCase();

    if ("QUIET".equals(warningLevelUpperCase)
        || "DEFAULT".equals(warningLevelUpperCase)
        || "VERBOSE".equals(warningLevelUpperCase)) {
      this.warningLevel = warningLevelUpperCase;
    } else {
      throw new BuildException("warningLevel expected to be one of "
          + " \"QUIET\", \"DEFAULT\", or \"VERBOSE\", "
          + "but was \"" + warningLevelUpperCase + "\"");
    }
  }


  // Nested element setters

  /**
   * @param compilerCheck a name-value pair where the name is a Closure
   *     Compiler check and the value is a check level. The supported checks
   *     correspond to the diagnostic groups defined in
   *     <a href="http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/DiagnosticGroups.java">
   *     DiagnosticGroups.java</a>. The check
   *     level must be one of "OFF", "WARNING", or "ERROR".
   * @throws BuildException if the check level is not a valid option
   */
  public void addConfiguredCompilerCheck(NameValuePair compilerCheck) {
    if (compilerCheck == null) {
      throw new BuildException("compilerCheck was null");
    }

    String checkLevelUpperCase = compilerCheck.getValue().toUpperCase();

    if ("OFF".equals(checkLevelUpperCase)
        || "WARNING".equals(checkLevelUpperCase)
        || "ERROR".equals(checkLevelUpperCase)) {

      this.compilerChecks.add(compilerCheck);
    } else {
      throw new BuildException("compiler check level expected to be one of "
          + " \"OFF\", \"WARNING\", or \"ERROR\", "
          + "but was \"" + checkLevelUpperCase + "\"");
    }
  }

  /**
   * For more information about compile-time defines, see: <a target="_blank"
   * href="http://www.amazon.com/gp/product/1449381871"><i>Closure: The
   * Definitive Guide</i></a> page 353.
   *
   * @param define a compile-time override for a JavaScript variable
   *     annotated with {@code @define}. The {@code type} field must be set
   *     to one of "boolean", "number", or "string". If the type is "string",
   *     then the {@code value} field will be automatically quoted.
   * @throws BuildException if the {@code name}, {@code value}, 
   *     or {@code type} field is {@code null}, or if the {@code type} field 
   *     is not one of "boolean", "number", or "string"  
   */
  public void addConfiguredDefine(Parameter define) {
    if (define == null) {
      throw new BuildException("define was null");
    }
    if (define.getName() == null) {
      throw new BuildException("<define> \"name\" attribute not set");
    }
    if (define.getValue() == null) {
      throw new BuildException("<define> \"value\" attribute not set");
    }
    if (define.getType() == null) {
      throw new BuildException("<define> \"type\" attribute not set; must "
          + "be one of \"boolean\", \"number\", or \"string\"");
    }
    
    this.defines.put(define);
  }

  /**
   * @param externs a file that contains externs that should be included in the
   *     compilation. By default, these will be used in addition to the default
   *     externs bundled with the Closure Compiler.
   */
  public void addExterns(FileSet externs) {
    this.externs.add(externs);
  }
}