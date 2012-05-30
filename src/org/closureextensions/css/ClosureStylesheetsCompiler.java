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

import com.google.common.css.ExitCodeHandler;
import com.google.common.css.JobDescription;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.commandline.DefaultCommandLineCompiler;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Wrapper for the Closure Stylesheets compiler to gain access to the protected
 * method {@link DefaultCommandLineCompiler#execute(java.io.File)}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ClosureStylesheetsCompiler
    extends DefaultCommandLineCompiler {

  private OutputRenamingMapFormat outputRenamingMapFormat;

  /**
   * Constructs a new Closure Stylesheets compiler wrapper to gain access to
   * {@link DefaultCommandLineCompiler#compile()}.
   */
  public ClosureStylesheetsCompiler(JobDescription job,
      ExitCodeHandler exitCodeHandler, ErrorManager errorManager) {
    super(job, exitCodeHandler, errorManager);
    this.outputRenamingMapFormat = OutputRenamingMapFormat.JSON;
  }

  /** @inheritDoc */
  @Override public String execute(@Nullable File renameFile) {
    return super.execute(renameFile);
  }

  /**
   * Set the {@link OutputRenamingMapFormat} used to write the renaming map.
   * This is only used if passing a renaming map file to {@link
   * #execute(java.io.File)}.
   *
   * @param renamingMapFormat the CSS renaming map format
   */
  public void setOutputRenamingMapFormat(
      OutputRenamingMapFormat renamingMapFormat) {
    this.outputRenamingMapFormat = renamingMapFormat;
  }

  /** @inheritDoc */
  @Override protected void writeRenamingMap(Map<String, String> renamingMap,
      PrintWriter renamingMapWriter)  {
    this.outputRenamingMapFormat.writeRenamingMap(renamingMap,
        renamingMapWriter);
  }
}
