/*
 * Copyright 2009 The Closure Compiler Authors.
 * Copyright (C) 2012 Christopher Peisert. All Rights Reserved.
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

package org.closureextensions.builderplus.cli;

import com.google.common.collect.Lists;

import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.closureextensions.builderplus.OutputMode;
import org.closureextensions.common.CssRenamingMap;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * Command line options for the Builder Plus command line interface.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class CommandLineOptions {

  @Option(name = "--css_renaming_map", usage = ""
      + "A file containing a JSON object\n"
      + "representing a CSS renaming map. Any\n"
      + "characters outside the outermost matching\n"
      + "set of curly braces are ignored. This\n"
      + "means that all of the CSS renaming map\n"
      + "output formats supported by Closure\n"
      + "Stylesheets (except PROPERTIES) may be\n"
      + "used. For Java Properties files, use\n"
      + "--css_renaming_map_properties_file\n")
  private File cssRenamingMapFile;

  @Option(name = "--css_renaming_map_properties_file", usage = ""
      + "A Java properties file containing\n"
      + "key-value pairs for a CSS renaming map.\n"
      + "See --css_renaming_map\n")
  private File cssRenamingMapPropertiesFile;

  @Option(name = "--compiler_jar", usage = ""
      + "The location of the Closure compiler .jar\n"
      + "file.")
  private File compilerJar;

  @Option(name = "--flagfile", usage = ""
      + "A file containing command line flags for\n"
      + "the Closure Compiler. The definitive list\n"
      + "of command line flags is defined in the\n"
      + "Compiler's CommandLineRunner.java.")
  private File flagFile;

  @Option(name = "--force_recompile", usage = ""
      + "Determines if the Closure Compiler should\n"
      + "always recompile the output file even if\n"
      + "none of the input files have changed\n"
      + "since the out file was last modified.\n"
      + "Defaults to false.", handler = BooleanOptionHandler.class)
  private boolean forceRecompile = false;

  @Option(name = "--help", aliases = {"--h", "-h"}, usage =
      "Print this message.")
  private boolean printHelp = false;

  @Option(name = "--input_manifest", usage = ""
      + "Specifies a file containing a list of\n"
      + "JavaScript sources to be included in the\n"
      + "compilation, where each line in the\n"
      + "manifest is a file path.")
  private File inputManifest;

  @Option(name = "--keep_all_sources", usage = ""
      + "Whether all sources should be passed to\n"
      + "the Closure Compiler (i.e., no sources\n"
      + "are pruned irrespective of the transitive\n"
      + "dependencies of the program entry points.)\n"
      + "This option is useful for compiling\n"
      + "libraries. Defaults to false.", handler = BooleanOptionHandler.class)
  private boolean keepAllSources = false;

  @Option(name = "--keep_moochers", usage = ""
      + "Whether \"moochers\" (i.e. source files\n"
      + "that do not provide any namespaces, though\n"
      + "they may goog.require namespaces) may be\n"
      + "dropped during the dependency pruning\n"
      + "process. If true, these files are always\n"
      + "kept as well as any files they depend on.\n"
      + "If false, these files may be dropped\n"
      + "during dependency pruning. Defaults to\n"
      + "false.", handler = BooleanOptionHandler.class)
  private boolean keepMoochers = false;

  @Option(name = "--keep_original_order", usage = ""
      + "Whether sources should be kept in their\n"
      + "original order or topologically sorted\n"
      + "based on their dependencies. Defaults to\n"
      + "false.", handler = BooleanOptionHandler.class)
  private boolean keepOriginalOrder = false;

  @Option(name = "--main_source", aliases = {"--ms", "-ms"}, usage = ""
      + "A source file that is a program entry\n"
      + "point. You may specify multiple.")
  private List<File> mainSources = Lists.newArrayList();

  @Option(name = "--namespace", usage = ""
      + "A namespace to calculate dependencies for\n"
      + "(i.e. a program entry point). The\n"
      + "namespace will be combined with those\n"
      + "goog.provided in the source files\n"
      + "specified with the --main_source flag. You\n"
      + "may specify multiple.")
  private List<String> namespaces = Lists.newArrayList();

  @Option(name = "--output_file", usage = ""
      + "The file to write output to instead of\n"
      + "standard output.")
  private File outputFile;

  @Option(name = "--output_manifest", usage = ""
      + "Prints out a list of all the files in the\n"
      + "compilation. This will not include files\n"
      + "that got dropped because they were not\n"
      + "required.")
  private File outputManifest;

  @Option(name = "--output_mode", usage = ""
      + "The output mode. Options: COMPILED,\n"
      + "MANIFEST, or RAW. Defaults to COMPILED.")
  private OutputMode outputMode = OutputMode.COMPILED;

  @Option(name = "--root", usage = ""
      + "A path to be recursively scanned for\n"
      + "source files. You may specify multiple.")
  private List<String> roots = Lists.newArrayList();

  @Option(name = "--source", aliases = {"--s", "-s", "--js", "-js"}, usage = ""
      + "A source file. You may specify multiple.")
  private List<File> sources = Lists.newArrayList();

  @Argument(metaVar = "[source [source2 [source3] ...]]", usage = ""
      + "Arguments without a flag are considered\n"
      + "additional source files. Equivalent to the\n"
      + "--source option.")
  private List<File> arguments = Lists.newArrayList();


  public CssRenamingMap getCssRenamingMap() throws CmdLineException {
    if (this.cssRenamingMapFile != null
        && this.cssRenamingMapPropertiesFile != null) {
      throw new CmdLineException("Only one of --css_renaming_map and "
          + "--css_renaming_map_properties_file may be specified.");
    }
    if (this.cssRenamingMapFile == null
        && this.cssRenamingMapPropertiesFile == null) {
      return null;
    }

    CssRenamingMap renamingMap;
    try {
      if (this.cssRenamingMapFile != null) {
        renamingMap = CssRenamingMap.createFromJsonFile(this.cssRenamingMapFile);
      } else {
        renamingMap = CssRenamingMap.createFromJavaPropertiesFile(
            this.cssRenamingMapPropertiesFile);
      }
    } catch (IOException e) {
      throw new CmdLineException(e);
    }

    return renamingMap;
  }

  public File getCompilerJar() {
    return compilerJar;
  }

  public File getFlagFile() {
    return flagFile;
  }

  public boolean isForceRecompile() {
    return forceRecompile;
  }

  public boolean isPrintHelp() {
    return printHelp;
  }

  public File getInputManifest() {
    return inputManifest;
  }

  public boolean isKeepAllSources() {
    return keepAllSources;
  }

  public boolean isKeepMoochers() {
    return keepMoochers;
  }

  public boolean isKeepOriginalOrder() {
    return keepOriginalOrder;
  }

  public List<File> getMainSources() {
    return mainSources;
  }

  public List<String> getNamespaces() {
    return namespaces;
  }

  public File getOutputFile() {
    return outputFile;
  }

  public File getOutputManifest() {
    return outputManifest;
  }

  public OutputMode getOutputMode() {
    return outputMode;
  }

  public List<String> getRoots() {
    return roots;
  }

  public List<File> getSources() {
    return sources;
  }

  public List<File> getArguments() {
    return arguments;
  }

  /**
   * OptionHandler for args4j that handles a boolean.
   *
   * <p>The difference between this handler and the default boolean option
   * handler supplied by args4j is that the default one doesn't take any
   * param, so can only be used to turn on boolean flags, but never to turn
   * them off.</p>
   *
   * <p>This class needs to be public due to reflection used by args4j.</p>
   */
  public static class BooleanOptionHandler extends OptionHandler<Boolean> {
    private static final Set<String> TRUES =
        Sets.newHashSet("true", "on", "yes", "1");
    private static final Set<String> FALSES =
        Sets.newHashSet("false", "off", "no", "0");

    public BooleanOptionHandler(
        CmdLineParser parser, OptionDef option,
        Setter<? super Boolean> setter) {
      super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
      String param = null;
      try {
        param = params.getParameter(0);
      } catch (CmdLineException e) {}

      if (param == null) {
        setter.addValue(true);
        return 0;
      } else {
        String lowerParam = param.toLowerCase();
        if (TRUES.contains(lowerParam)) {
          setter.addValue(true);
        } else if (FALSES.contains(lowerParam)) {
          setter.addValue(false);
        } else {
          setter.addValue(true);
          return 0;
        }
        return 1;
      }
    }

    @Override
    public String getDefaultMetaVariable() {
      return null;
    }
  }
}

