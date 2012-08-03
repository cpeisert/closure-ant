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
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;

import org.closureant.types.NameValuePair;
import org.closureant.util.AntUtil;
import org.closureant.util.StringUtil;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Builder to create command lines for shell execution.
 *
 * <p>The {@code CommandLineBuilder} API makes a distinction between command
 * line flags such as {@code --help} and other non-flag command line arguments,
 * which are simply referred to as {@code arguments}. The motivation for
 * this design is to handle the case where the same flag is applied to
 * multiple arguments as well as separately controlling if flags and/or
 * arguments should be quoted.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class CommandLineBuilder {
  private final List<CommandLineParameter> cmdline;
  private boolean quoteFlags;
  private boolean quoteArguments;
  
  /**
   * Constructs a new CommandLineBuilder.
   */
  public CommandLineBuilder() {
    this.cmdline = Lists.newArrayListWithExpectedSize(64);
    this.quoteFlags = false;
    this.quoteArguments = false;
  }

  /**
   * Determine if command line flags should be quoted. If flags should be 
   * quoted, the selection of either double or single quotes for each 
   * flag works as follows:
   * 
   * <p><ul>
   * <li><i>double quotes</i> if a flag contains no quotation marks or only 
   * single quote marks</li>
   * <li><i>single quotes</i> if a flag contains double quote marks</li>
   * <li><i>AssertionError</i> if a flag contains both double and single 
   * quotation marks</li>
   * </ul></p>
   * 
   * <p><i>Note:</i> if a flag is already quoted, additional quotation marks 
   * are not added.</p>  
   *
   * @param quoteFlags determines if command line flags should be quoted
   * @return {@code this}
   */
  public CommandLineBuilder quoteFlags(boolean quoteFlags) {
    this.quoteFlags = quoteFlags;
    return this;
  }

  /**
   * Determine if command line arguments should be quoted. If arguments should 
   * be quoted, the selection of either double or single quotes for each 
   * argument works as follows:
   *
   * <p><ul>
   * <li><i>double quotes</i> if an argument contains no quotation marks or 
   * only single quote marks</li>
   * <li><i>single quotes</i> if an argument contains double quote marks</li>
   * <li><i>AssertionError</i> if an argument contains both double and single 
   * quotation marks</li>
   * </ul></p>
   *
   * <p><i>Note:</i> if an argument is already quoted, additional quotation 
   * marks are not added.</p>  
   *  
   * @param quoteArguments determines if command line arguments should be 
   *     quoted
   * @return {@code this}
   */
  public CommandLineBuilder quoteArguments(boolean quoteArguments) {
    this.quoteArguments = quoteArguments;
    return this;
  }
  
  /**
   * Add a single command-line argument. The argument may contain spaces but
   * may not contain both double and single quotes to simplify
   * cross-platform quoting.
   *
   * @param arg the command line argument to add
   * @return {@code this}
   * @throws NullPointerException if {@code arg} is {@code null}
   * @throws AssertionError if {@code arg} contains both double and single 
   *     quotes
   */
  public <T> CommandLineBuilder argument(T arg) {
    Preconditions.checkNotNull(arg, "arg was null");
    
    String argument = arg.toString();
    if (!argument.trim().isEmpty()) {
      this.cmdline.add(new Argument(argument));
    }
    return this;
  }

  /**
   * Add a single command-line argument along with a command line flag. The 
   * argument may contain spaces but may not contain both double and single
   * quotes to simplify cross-platform quoting.
   *
   * @param flag a command line flag for each argument
   * @param arg the command line argument to add
   * @return {@code this}
   * @throws NullPointerException if {@code arg} or {@code flag} is {@code null}
   * @throws AssertionError if {@code arg} or {@code flag} contain both double 
   *     and single quotes
   */
  public <T> CommandLineBuilder flagAndArgument(String flag, T arg) {
    Preconditions.checkNotNull(flag, "flag was null");
    Preconditions.checkNotNull(arg, "arg was null");
    
    if (!flag.trim().isEmpty()) {
      this.cmdline.add(new Flag(flag));
    }
    
    String argument = arg.toString();
    if (!argument.trim().isEmpty()) {
      this.cmdline.add(new Argument(argument));
    }
    return this;
  }

  /**
   * Add command-line arguments. The arguments may contain spaces but
   * may not contain both double and single quotes to simplify
   * cross-platform quoting.
   *
   * @param args the command line arguments to add
   * @return {@code this}
   * @throws NullPointerException if any of the {@code args} is {@code null}
   * @throws AssertionError if an {@code arg} contains both double and single 
   *     quotes
   */
  public <T> CommandLineBuilder arguments(Iterable<T> args) {
    Preconditions.checkNotNull(args, "args was null");

    for (T arg : args) {
      Preconditions.checkNotNull(arg, "args contained null element");
      if (!arg.toString().isEmpty()) {
        this.cmdline.add(new Argument(arg.toString()));
      }
    }
    return this;
  }

  /**
   * Add command-line arguments. The arguments may contain spaces but
   * may not contain both double and single quotes to simplify
   * cross-platform quoting. The parameter {@code toStringFunction} allows you
   * to provide a custom string conversion function for {@code args}. For
   * example, if {@code args} were a {@link List} of {@link File}, then a
   * {@code toStringFunction} could be provided that used 
   * {@link java.io.File#getAbsolutePath()} rather than the default {@link 
   * java.io.File#toString()}.
   *
   * @param args the command line arguments to add
   * @param toStringFunction a function object to convert an argument from 
   *     the actual type parameter of {@code args} to {@link String}
   * @return {@code this}
   * @throws NullPointerException if any of the arguments is {@code null}
   * @throws AssertionError if an {@code arg} contains both double and single 
   *     quotes
   */
  public <T> CommandLineBuilder arguments(Iterable<T> args,
      Function<T, String> toStringFunction) {
    Preconditions.checkNotNull(args, "args was null");
    Preconditions.checkNotNull(toStringFunction, "toStringFunction was null");

    for (T arg : args) {
      Preconditions.checkNotNull(arg, "args contained null element");
      if (!arg.toString().isEmpty()) {
        this.cmdline.add(new Argument(toStringFunction.apply(arg)));
      }
    }
    return this;
  }

  /**
   * Add command-line arguments along with a command line flag to be
   * associated with each argument. The arguments may contain spaces but may
   * not contain both double and single quotes to simplify cross-platform
   * quoting.
   *
   * @param flag a command line flag for each argument
   * @param args the command line arguments to add
   * @return {@code this}
   * @throws NullPointerException if any of the {@code args} or {@code flag}
   *     is {@code null}
   * @throws AssertionError if an {@code arg} or {@code flag} contain both
   *     double and single quotes
   */
  public <T> CommandLineBuilder flagAndArguments(String flag,
                                                 Iterable<T> args) {
    Preconditions.checkNotNull(flag, "flag was null");
    Preconditions.checkNotNull(args, "args was null");

    for (T arg : args) {
      Preconditions.checkNotNull(arg, "args contained null element");
      if (!arg.toString().isEmpty()) {
        if (!flag.isEmpty()) {
          this.cmdline.add(new Flag(flag));
        }
        this.cmdline.add(new Argument(arg.toString()));
      }
    }
    return this;
  }

  /**
   * Add command-line arguments along with a command line flag to be
   * associated with each argument. The arguments may contain spaces but
   * may not contain both double and single quotes to simplify cross-platform
   * quoting. The parameter {@code toStringFunction} allows you to provide a 
   * custom string conversion function for {@code args}. For example, if
   * {@code args} were a {@link List} of {@link File}, then a
   * {@code toStringFunction} could be provided that used
   * {@link java.io.File#getAbsolutePath()} rather than the default
   * {@link java.io.File#toString()}.
   *
   * @param flag a command line flag for each argument
   * @param args the command line arguments to add
   * @param toStringFunction a function object to convert an argument from 
   *     the actual type parameter of {@code args} to {@link String}
   * @return {@code this}
   * @throws NullPointerException if any of the arguments is {@code null}
   * @throws AssertionError if an {@code arg} or {@code flag} contain both
   *     double and single quotes
   */
  public <T> CommandLineBuilder flagAndArguments(String flag,
      Iterable<T> args, Function<T, String> toStringFunction) {
    Preconditions.checkNotNull(flag, "flag was null");
    Preconditions.checkNotNull(args, "args was null");
    Preconditions.checkNotNull(toStringFunction, "toStringFunction was null");

    for (T arg : args) {
      Preconditions.checkNotNull(arg, "args contained null element");
      if (!arg.toString().isEmpty()) {
        if (!flag.isEmpty()) {
          this.cmdline.add(new Flag(flag));
        }
        this.cmdline.add(new Argument(toStringFunction.apply(arg)));
      }
    }
    return this;
  }

  /**
   * Add command-line arguments for each {@code File} contained in an Ant 
   * {@link AbstractFileSet}, which may either be a {@code FileSet} or {@code
   * DirSet}.  
   *
   * @param fileSet the {@code AbstractFileSet}
   * @param project the Ant project
   * @return {@code this}
   * @throws AssertionError if a file path of a file in {@code fileSet}
   *     contains both double and single quotes
   * @throws NullPointerException if {@code fileSet} is {@code null}
   */
  public CommandLineBuilder fileSet(AbstractFileSet fileSet, Project project) {
    Preconditions.checkNotNull(fileSet, "fileSet was null");
    Preconditions.checkNotNull(project, "project was null");

    List<File> files = AntUtil.getListOfFilesFromAntFileSet(project,
        fileSet);
    return this.arguments(files,
        new Function<File, String>() {
          public String apply(@Nullable File file) {
            return file.getAbsolutePath();
          }
        }
    );
  }

  /**
   * Add command-line arguments for each {@code File} contained in an Ant 
   * {@link AbstractFileSet} along with a flag to be associated with each
   * file path. Note: an {@code AbstractFileSet} may be either a
   * {@code FileSet} or {@code DirSet}.
   *
   * @param flag a command line flag for each argument
   * @param fileSet the {@code AbstractFileSet}
   * @param project the Ant project
   * @return {@code this}
   * @throws NullPointerException if {@code flag} or {@code fileSet} is
   *     {@code null}
   * @throws AssertionError if a file path of a file in {@code fileSet} 
   *     contains both double and single quotes
   */
  public CommandLineBuilder flagAndFileSet(String flag,
      AbstractFileSet fileSet, Project project) {
    Preconditions.checkNotNull(flag, "flag was null");
    Preconditions.checkNotNull(fileSet, "fileSet was null");

    List<File> files = AntUtil.getListOfFilesFromAntFileSet(project,
        fileSet);
    return this.flagAndArguments(flag, files,
        new Function<File, String>() {
          public String apply(@Nullable File file) {
            return file.getAbsolutePath();
          }
        }
    );
  }

  /**
   * Add a {@link CommandLineParameter} to the command line. See
   * {@link CommandLineParameter}. This method is to facilitate efficient 
   * copying of one {@link CommandLineBuilder} to another. 
   * 
   * @param param the command line parameter
   * @return {@code this}
   */
  private CommandLineBuilder commandLineParameter(CommandLineParameter param) {
    Preconditions.checkNotNull(param, "param was null");
    if (param.isFlag()) {
      this.cmdline.add(new Flag(param.getValue()));
    } else {
      this.cmdline.add(new Argument(param.getValue()));
    }
    return this;
  }

  /**
   * Add {@link CommandLineBuilder.CommandLineParameter} elements to this
   * {@code CommandLineBuilder}.
   *
   * @param builder the command line builder to copy from
   * @return {@code this}
   */
  public CommandLineBuilder commandLineBuilder(CommandLineBuilder builder) {
    for (CommandLineParameter param : builder.cmdline) {
      this.commandLineParameter(param);
    }
    return this;
  }
  
  /**
   * Return the command line as a String array compatible with Ant {@link 
   * org.apache.tools.ant.types.Commandline} and 
   * {@link org.apache.tools.ant.taskdefs.Execute}.
   * 
   * @return the command line as a String array, where each command line 
   *     argument and flag are stored as separate elements
   */
  public String[] toStringArray() {
    List<String> list = this.toListOfString();
    return list.toArray(new String[0]);
  }

  /**
   * Return the command line as a String. Each command line argument and flag
   * is separated with a space.
   *
   * @return the command line as a String where each command line
   *     argument and flag is separated by a space
   */
  public String toString() {
    List<String> list = this.toListOfString();
    return Joiner.on(" ").join(list);
  }

  /**
   * Return the command line as a string with each command line flag 
   * and its adjacent argument separated by {@code separator} and other 
   * arguments separated by a space.
   *
   * @param separator the character to insert between flags and their 
   *     adjacent arguments
   * @return the command line as a string
   */
  public String toStringWithFlagsAndArgumentsJoinedOn(char separator) {    
    return toStringWithFlagsAndArgumentsJoinedOn(Character.toString(separator));
  }

  /**
   * Return the command line as a string with each command line flag 
   * and its adjacent argument separated by {@code separator} and other 
   * arguments separated by a space.
   *
   * @param separator the string to insert between flags and their 
   *     adjacent arguments
   * @return the command line as a string
   */
  public String toStringWithFlagsAndArgumentsJoinedOn(String separator) {
    Preconditions.checkNotNull(separator, "separator was null");

    StringBuilder builder = new StringBuilder();

    // Note: assumes that a flag is always followed by a command line
    // argument, since CommandLineBuilder does provide a method for adding a
    // flag without a corresponding argument
    for (CommandLineParameter param : this.cmdline) {
      builder.append(param.getValue());
      if (param instanceof Flag) {
        builder.append(separator);
      } else {
        builder.append(" ");
      }
    }
    if (builder.charAt(builder.length() - 1) == ' ') {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  /**
   * Return the command line as a {@link List} of {@code String}, where each
   * command line argument and flag are separate elements.
   *
   * @return a list of {@code String} containing the command line flags and 
   *     arguments
   */
  public List<String> toListOfString() {
    List<String> list = Lists.newArrayListWithCapacity(this.cmdline.size());

    for (CommandLineParameter param : this.cmdline) {
      list.add(param.getValue());
    }
    return list;
  }

  /**
   * Get the command-line flags and their associated values as a list of
   * {@link org.closureant.types.NameValuePair}. Command-line arguments that are not preceded by a
   * flag are not included in the returned list.
   *
   * <p>The union of the results returned by
   * {@link #getFlagsAsListOfNameValuePair()} and
   * {@link #getArgumentsNotPrecededByFlags()} comprise the set of all
   * command-line parameters set in this {@link CommandLineBuilder}.</p>
   *
   * @return a list of the command-line flags and their associated values
   */
  public List<NameValuePair> getFlagsAsListOfNameValuePair() {
    List<NameValuePair> flagPairs = Lists.newArrayList();

    for (Iterator<CommandLineParameter> i = cmdline.iterator(); i.hasNext(); ) {
      CommandLineParameter param = i.next();
      if (param.isFlag()) {
        NameValuePair flagPair = new NameValuePair();
        flagPair.setName(param.getValue());
        flagPair.setValue(i.next().getValue());
        flagPairs.add(flagPair);
      }
    }
    return flagPairs;
  }

  /**
   * Get the command-line arguments that are not preceded by a flag.
   * Command-line arguments preceded by a flag are not included in the
   * returned list.
   *
   * <p>The union of the results returned by
   * {@link #getFlagsAsListOfNameValuePair()} and
   * {@link #getArgumentsNotPrecededByFlags()} comprise the set of all
   * command-line parameters set in this {@link CommandLineBuilder}.</p>
   *
   * <p>Example:</p>
   *
   * <p>{@code java -jar compiler.jar --js myapp.js}</b></p>
   *
   * <p>The argument {@code myapp.js} would not be returned because it is
   * preceded by the {@code --js} flag.</p>
   *
   * @return a list of the command-line flags and their associated values
   */
  public List<String> getArgumentsNotPrecededByFlags() {
    List<String> arguments = Lists.newArrayList();

    for (Iterator<CommandLineParameter> i = cmdline.iterator(); i.hasNext(); ) {
      CommandLineParameter param = i.next();
      if (param.isFlag()) {
        i.next(); // skip argument following flag
      } else {
        arguments.add(param.getValue());
      }
    }
    return arguments;
  }

  /**
   * An object representing a command line parameter such as a flag or an 
   * argument.
   */
  private interface CommandLineParameter {
    /**
     * Returns the string value of the command line parameter (that is, 
     * the value of a flag or an argument). If the {@code CommandLineParameter} 
     * is a {@code Flag} and {@link CommandLineBuilder#quoteFlags} is {@code 
     * true}, or the {@code CommandLineParameter} is an {@code Argument} and 
     * {@link CommandLineBuilder#quoteArguments} is {@code true}, then the
     * value is quoted based on the rules below. Otherwise, the value is 
     * returned unchanged from when it was most recently set.  
     * 
     * <p><ul>
     * <li><i>double quotes</i> if a parameter contains no quotation marks or 
     * only single quotation marks</li>
     * <li><i>single quotes</i> if a parameter contains double quote marks</li>
     * <li><i>AssertionError</i> if a parameter contains both double and single 
     * quotation marks</li>
     * </ul></p>
     *
     * <p><i>Note:</i> if a parameter is already quoted, additional quotation 
     * marks are not added.</p>  
     *
     * @return the string value of the command line parameter
     * @throws AssertionError if the command line parameter value contains
     *     both double and single quotes
     */
    String getValue();

    /**
     * @return {@code true} if this {@link CommandLineParameter} was added to
     * the {@link CommandLineBuilder} as a flag rather than an argument. See 
     * {@link CommandLineBuilder}.
     */
    boolean isFlag();
  }
  
  private final class Flag implements CommandLineParameter {
    private final String value;
    
    Flag(String value) {
      Preconditions.checkNotNull(value, "value was null");
      if (StringUtil.stringContainsDoubleAndSingleQuotes(value)) {
        throw new AssertionError("value: [" + value + "] contains both double "
            + "and single quotes");
      }
      this.value = value;
    }
    
    public String getValue() {
      if (quoteFlags) {
        return StringUtil.quoteString(this.value);        
      } else {
        return this.value;
      }
    }
    
    public boolean isFlag() {
      return true;
    }
  }

  private final class Argument implements CommandLineParameter {
    private final String value;
    
    Argument(String value) {
      Preconditions.checkNotNull(value, "value was null");
      if (StringUtil.stringContainsDoubleAndSingleQuotes(value)) {
        throw new AssertionError("value: [" + value + "] contains both double "
            + "and single quotes");
      }
      this.value = value;
    }
    
    public String getValue() {
      if (quoteArguments) {
        return StringUtil.quoteString(this.value);
      } else {
        return this.value;
      }
    }
    
    public boolean isFlag() {
      return false;
    }
  }
}

