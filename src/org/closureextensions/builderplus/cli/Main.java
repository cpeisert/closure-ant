/*
 * Copyright 2009 The Closure Compiler Authors.
 * Copyright (C) 2012 Christopher Peisert.
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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry point for Builder Plus command line interface.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class Main {

  public static void main(String[] args) throws CmdLineException, IOException {
    List<String> processedArgs = processArgs(args);

    CommandLineOptions options = new CommandLineOptions();
    CmdLineParser parser = new CmdLineParser(options);
    boolean optionsValid = true;

    try {
      parser.parseArgument(processedArgs.toArray(new String[] {}));
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      optionsValid = false;
    }

    if (!optionsValid || options.isPrintHelp()) {
      System.err.println("java -jar builderplus.jar [options] "
          + "[source [source2 [source3] ...]]" + String.format("%n")
          + "Options:");
      parser.printUsage(System.err);
    } else {
      BuilderPlusRunner runner = new BuilderPlusRunner(options);
      runner.execute();
    }
  }

  // Function from Closure Compiler CommandLineRunner.java
  private static List<String> processArgs(String[] args) {
    // Args4j has a different format that the old command-line parser.
    // So we use some voodoo to get the args into the format that args4j
    // expects.
    Pattern argPattern = Pattern.compile("(--[a-zA-Z_]+)=(.*)");
    Pattern quotesPattern = Pattern.compile("^['\"](.*)['\"]$");
    List<String> processedArgs = Lists.newArrayList();

    for (String arg : args) {
      Matcher matcher = argPattern.matcher(arg);
      if (matcher.matches()) {
        processedArgs.add(matcher.group(1));

        String value = matcher.group(2);
        Matcher quotesMatcher = quotesPattern.matcher(value);
        if (quotesMatcher.matches()) {
          processedArgs.add(quotesMatcher.group(1));
        } else {
          processedArgs.add(value);
        }
      } else {
        processedArgs.add(arg);
      }
    }

    return processedArgs;
  }
}

