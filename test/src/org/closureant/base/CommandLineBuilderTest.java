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

package org.closureant.base;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.types.FileSet;

import org.closureant.MockProject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.closureant.base.CommandLineBuilder}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class CommandLineBuilderTest {
  private CommandLineBuilder cmdline;
  private final String executable = "python";
  
  @Before public void setUp() {
    cmdline = new CommandLineBuilder();
    cmdline.argument(executable);
  }

  @Test public void simpleCommandLineWithNoArguments() {
    assertEquals(this.executable, this.cmdline.toString());
  }

  @Test public void quoteFlags() {
    this.cmdline.flagAndArgument("--flag", "arg01").quoteFlags(true);
    assertEquals(this.executable + " \"--flag\" arg01",
        this.cmdline.toString());
  }

  @Test public void quoteArguments() {
    this.cmdline.quoteArguments(true);
    assertEquals("\"" + this.executable + "\"", this.cmdline.toString());
  }

  // CommandLineBuilder.argument(T arg)

  @Test public void argumentSimple() {
    assertEquals(this.executable + " arg01",
        this.cmdline.argument("arg01").toString());
  }

  @Test public void argumentEmptyString() {
    assertEquals(this.executable,
        this.cmdline.argument("").toString());
  }
  
  @Test(expected = NullPointerException.class)
  public void argumentNull() {
    this.cmdline.argument(null);
  }

  @Test(expected = AssertionError.class)
  public void argumentContainsDoubleAndSingleQuotes() {
    this.cmdline.argument("double \" and single '");
  }

  // CommandLineBuilder.flagAndArgument(String flag, T arg)

  @Test public void flagAndArgumentSimple() {
    assertEquals(this.executable + " --flag arg01",
        this.cmdline.flagAndArgument("--flag", "arg01").toString());
  }

  @Test public void flagAndArgumentEmptyArgs() {
    assertEquals(this.executable,
        this.cmdline.flagAndArgument("", "").toString());
  }

  @Test(expected = NullPointerException.class)
  public void flagAndArgumentNullFlag() {
    this.cmdline.flagAndArgument(null, "arg01");
  }

  @Test(expected = NullPointerException.class)
  public void flagAndArgumentNullArg() {
    this.cmdline.flagAndArgument("--flag", null);
  }

  @Test(expected = AssertionError.class)
  public void flagAndArgumentArgContainsDoubleAndSingleQuotes() {
    this.cmdline.flagAndArgument("--flag", "double \" and single '");
  }

  // CommandLineBuilder.arguments(Iterable<T> args)

  @Test public void argumentsSimple() {
    List<String> args = Lists.newArrayList("arg01", "arg02");
    assertEquals(this.executable + " arg01 arg02",
        this.cmdline.arguments(args).toString());
  }

  @Test public void argumentsArgsWithEmptyString() {
    List<String> args = Lists.newArrayList("");    
    assertEquals(this.executable,
        this.cmdline.arguments(args).toString());
  }

  @Test(expected = NullPointerException.class)
  public void argumentsNullIterable() {
    this.cmdline.arguments(null);
  }

  @Test(expected = NullPointerException.class)
  public void argumentsIterableContainsNullArg() {
    List<String> args = Lists.newArrayList();
    args.add(null);
    this.cmdline.arguments(args);
  }

  @Test(expected = AssertionError.class)
  public void argumentsContainsArgWithDoubleAndSingleQuotes() {
    List<String> args = Lists.newArrayList("double \" and single '");
    this.cmdline.arguments(args);
  }

  // CommandLineBuilder.arguments(Iterable<T> args, 
  //     Function<T, String> toStringFunction)

  @Test public void argumentsWithToStringFunctionSimple() {
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    List<String> args = Lists.newArrayList("arg01");
    assertEquals(this.executable + " arg01",
        this.cmdline.arguments(args, myToString).toString());
  }

  @Test public void argumentsWithToStringFunctionArgsWithEmptyString() {
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    List<String> args = Lists.newArrayList("");
    assertEquals(this.executable,
        this.cmdline.arguments(args, myToString).toString());
  }

  @Test(expected = NullPointerException.class)
  public void argumentsWithToStringFunctionNullIterable() {
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    this.cmdline.arguments(null, myToString);
  }

  @Test(expected = NullPointerException.class)
  public void argumentsWithToStringFunctionIterableContainsNullArg() {
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    List<String> args = Lists.newArrayList();
    args.add(null);
    this.cmdline.arguments(args, myToString);
  }

  @Test(expected = AssertionError.class)
  public void argumentsWithToStringFunctionContainsArgWithDoubleAndSingleQuotes() {
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    List<String> args = Lists.newArrayList("double \" and single '");
    this.cmdline.arguments(args, myToString);
  }

  // CommandLineBuilder.flagAndArguments(String flag, Iterable<T> args)

  @Test public void flagAndArgumentsSimple() {
    List<String> args = Lists.newArrayList("arg01", "arg02");
    assertEquals(this.executable + " --flag arg01 --flag arg02",
        this.cmdline.flagAndArguments("--flag", args).toString());
  }

  @Test public void flagAndArgumentsEmptyFlagAndArgsWithEmptyArg() {
    List<String> args = Lists.newArrayList("");
    assertEquals(this.executable,
        this.cmdline.flagAndArguments("", args).toString());
  }

  @Test(expected = NullPointerException.class)
  public void flagAndArgumentsNullFlag() {
    List<String> args = Lists.newArrayList("arg01");
    this.cmdline.flagAndArguments(null, args);
  }

  @Test(expected = NullPointerException.class)
  public void flagAndArgumentsNullArgs() {
    this.cmdline.flagAndArguments("--flag", null);
  }

  @Test(expected = AssertionError.class)
  public void flagAndArgumentsArgsContainsArgWithDoubleAndSingleQuotes() {
    List<String> args = Lists.newArrayList("double \" and single '");
    this.cmdline.flagAndArguments("--flag", args);
  }

  // CommandLineBuilder.flagAndArguments(String flag,
  //     Iterable<T> args, Function<T, String> toStringFunction)

  @Test public void flagAndArgumentsWithToStringFunctionSimple() {
    List<String> args = Lists.newArrayList("arg01", "arg02");
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    assertEquals(this.executable + " --flag arg01 --flag arg02",
        this.cmdline.flagAndArguments("--flag", args, myToString).toString());
  }

  @Test public void flagAndArgumentsWithToStringFunctionEmptyFlagAndArgsWithEmptyArg() {
    List<String> args = Lists.newArrayList("");
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    assertEquals(this.executable,
        this.cmdline.flagAndArguments("", args, myToString).toString());
  }

  @Test(expected = NullPointerException.class)
  public void flagAndArgumentsWithToStringFunctionNullFlag() {
    List<String> args = Lists.newArrayList("arg01");
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    this.cmdline.flagAndArguments(null, args, myToString);
  }

  @Test(expected = NullPointerException.class)
  public void flagAndArgumentsWithToStringFunctionNullArgs() {
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    this.cmdline.flagAndArguments("--flag", null, myToString);
  }

  @Test(expected = NullPointerException.class)
  public void flagAndArgumentsWithToStringFunctionNullFunction() {
    List<String> args = Lists.newArrayList("arg01");
    this.cmdline.flagAndArguments("--flag", args, null);
  }

  @Test(expected = AssertionError.class)
  public void flagAndArgumentsWithToStringFunctionArgsContainsArgWithDoubleAndSingleQuotes() {
    List<String> args = Lists.newArrayList("double \" and single '");
    Function<String, String> myToString = new Function<String, String>() {
      public String apply(String string) {
        return string;
      }
    };
    this.cmdline.flagAndArguments("--flag", args, myToString);
  }

  // CommandLineBuilder.fileSet(AbstractFileSet fileSet)

  @Test public void fileSetSimple() {
    FileSet fileSet = new FileSet();
    fileSet.setProject(MockProject.getProject());
    
    File file = new File("file.tmp");
    try {
      if (!file.createNewFile()) {
        throw new IOException("unable to create file ["
            + file.getAbsolutePath() + "]");
      }
      fileSet.setFile(file);
      fileSet.setDir(new File("."));
      this.cmdline.fileSet(fileSet, MockProject.getProject());

      String expected = this.executable + " " + file.getAbsolutePath();
      String actual = this.cmdline.toString();
      assertEquals(expected.toLowerCase(), actual.toLowerCase());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    } finally {
      file.delete();
    }
  }
  
  @Test(expected = NullPointerException.class)
  public void fileSetNullFileSet() {        
    this.cmdline.fileSet(null, MockProject.getProject());
  }

  // CommandLineBuilder.flagAndFileSet(String flag, AbstractFileSet fileSet)

  @Test public void flagAndFileSetSimple() {
    FileSet fileSet = new FileSet();
    fileSet.setProject(MockProject.getProject());

    File file = new File("file.tmp");
    try {
      if (!file.createNewFile()) {
        throw new IOException("unable to create file ["
            + file.getAbsolutePath() + "]");
      }
      fileSet.setFile(file);
      fileSet.setDir(new File("."));
      this.cmdline.flagAndFileSet("--flag", fileSet, MockProject.getProject());

      String expected = this.executable + " --flag " + file.getAbsolutePath();
      String actual = this.cmdline.toString();
      assertEquals(expected.toLowerCase(), actual.toLowerCase());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    } finally {
      file.delete();
    }
  }

  @Test(expected = NullPointerException.class)
  public void flagAndFileSetNullFlag() {
    this.cmdline.flagAndFileSet(null, new FileSet(), MockProject.getProject());
  }

  @Test(expected = NullPointerException.class)
  public void flagAndFileSetNullFileSet() {
    this.cmdline.flagAndFileSet("--flag", null, MockProject.getProject());
  }

  // CommandLineBuilder conversion functions

  @Test public void toStringArraySimple() {
    this.cmdline.flagAndArgument("--flag", "arg01");
    String[] expectedCmdline = {this.executable, "--flag", "arg01"};
    assertTrue(Arrays.deepEquals(expectedCmdline,
        this.cmdline.toStringArray()));
  }

  @Test public void toStringSimple() {
    this.cmdline.flagAndArgument("--flag", "arg01");
    String expectedCmdline = this.executable + " --flag arg01";
    assertEquals(expectedCmdline, this.cmdline.toString());
  }
  
  @Test public void toStringWithFlagsAndArgumentsJoinedOnEquals() {
    this.cmdline.flagAndArgument("--flag01", "arg01");
    String expectedCmdline = this.executable + " --flag01=arg01";
    assertEquals(expectedCmdline,
        this.cmdline.toStringWithFlagsAndArgumentsJoinedOn("="));

    this.cmdline.flagAndArgument("--flag02", "arg02");
    expectedCmdline = expectedCmdline + " --flag02=arg02";
    assertEquals(expectedCmdline,
        this.cmdline.toStringWithFlagsAndArgumentsJoinedOn("="));
  }

  @Test public void toListSimple() {
    this.cmdline.flagAndArgument("--flag", "arg01");
    List<String> expectedCmdline = new ArrayList<String>();
    expectedCmdline.add(this.executable);
    expectedCmdline.add("--flag");
    expectedCmdline.add("arg01");
    assertEquals(expectedCmdline, this.cmdline.toListOfString());
  }
}