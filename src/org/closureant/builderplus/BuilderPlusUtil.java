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

package org.closureant.builderplus;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.closureant.types.CompilationLevel;
import org.closureant.css.CssRenamingMap;
import org.closureant.base.JsClosureSourceFile;
import org.closureant.base.SourceFileFactory;

/**
 * Static utility functions shared by Builder Plus' Ant task and
 * command-line-interface APIs.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class BuilderPlusUtil {
  private BuilderPlusUtil() {}

  /**
   * Creates a temporary CSS renaming map file in the specified output
   * directory using either the Closure Stylesheets renaming map format
   * CLOSURE_COMPILED or CLOSURE_UNCOMPILED. See {@link
   * org.closureant.BuilderPlusTask#setCssRenamingMap(String)}.
   *
   * <p>The temporary renaming map file is then added to the manifest list
   * either immediately before or after Closure's base.js (CLOSURE_COMPILED
   * after base.js and CLOSURE_UNCOMPILED before). If base.js is not present,
   * the renaming map is inserted at the head of the list.</p>
   *
   * @param renamingMap CSS renaming map to write to temp file
   * @param builderPlusMode the BuilderPlus output mode
   * @param compilationLevel Closure Compiler compilation level
   * @param manifestList the manifest list
   * @param outputDirectory the directory to save the temp CSS renaming map file
   * @return the temporary CSS renaming map file
   * @throws IOException if unable to create temp CSS renaming map file
   * @throws NullPointerException if any of the arguments are {@code null}
   */
  public static JsClosureSourceFile createRenamingMapFileAndAddToManifest(
      CssRenamingMap renamingMap, OutputMode builderPlusMode,
      CompilationLevel compilationLevel,
      List<JsClosureSourceFile> manifestList, File outputDirectory)
      throws IOException {

    Preconditions.checkNotNull(renamingMap, "renamingMap is null");
    Preconditions.checkNotNull(builderPlusMode, "builderPlusMode is null");
    Preconditions.checkNotNull(compilationLevel, "compilationLevel is null");
    Preconditions.checkNotNull(manifestList, "manifestList is null");

    JsClosureSourceFile tempRenamingMapFile;
    File file;
    boolean beforeBaseJs = false;

    if (builderPlusMode.equals(OutputMode.RAW)
        || compilationLevel.equals(CompilationLevel.WHITESPACE_ONLY)) {
      beforeBaseJs = true;
      file = new File(outputDirectory,"css_renaming_map_CLOSURE_UNCOMPILED.js");
      writeRenamingMap(renamingMap, "CLOSURE_CSS_NAME_MAPPING = %s;\n",file);
    } else {
      file = new File(outputDirectory, "css_renaming_map_CLOSURE_COMPILED.js");
      writeRenamingMap(renamingMap, "goog.setCssNameMapping(%s);\n", file);
    }

    tempRenamingMapFile = SourceFileFactory.newJsClosureSourceFile(file);
    boolean added = false;

    for (ListIterator<JsClosureSourceFile> i = manifestList.listIterator();
         i.hasNext();) {
      JsClosureSourceFile sourceFile = i.next();
      if (sourceFile.isBaseJs()) {
        if (beforeBaseJs) {
          i.previous();
        }
        i.add(tempRenamingMapFile);
        added = true;
        break;
      }
    }
    if (!added) {
      manifestList.add(0, tempRenamingMapFile);
    }

    return tempRenamingMapFile;
  }

  /**
   * Based on {@link
   * com.google.common.css.compiler.commandline.OutputRenamingMapFormat}.
   */
  private static void writeRenamingMap(CssRenamingMap renamingMap,
      String formatString, File outputFile) throws IOException {
    // Build up the renaming map as a JsonObject.
    JsonObject properties = new JsonObject();
    for (Map.Entry<String, String> entry : renamingMap.entrySet()) {
      properties.addProperty(entry.getKey(), entry.getValue());
    }

    // Write the JSON wrapped in this output format's formatString.
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Files.write(String.format(formatString, gson.toJson(properties)),
        outputFile, Charsets.UTF_8);
  }
}
