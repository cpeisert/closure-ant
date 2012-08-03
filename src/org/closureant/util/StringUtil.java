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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for specialized String functions.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class StringUtil {
  private StringUtil() {}

  /**
   * Indicates if a string contains both double and single quotation marks,
   * not including matching pairs of leading and trailing quote marks.
   *
   * <p><b>Examples</b></p>
   *
   * <p><ul>
   * <li>['Single quoted strings are "common" in JavaScript.'] - returns
   * {@code false}</li>
   * <li>[Isn't it \"grand\"?] - returns {@code true}</li>
   * <li>["Howdy y'all!"] - returns {@code false}</li>
   * <li>["She said, "Make my day.""] - returns {@code false}</li>
   * </ul></p>
   *
   * @param string the string to check
   * @return {@code true} if {@code string} contains both double (") and
   *     single(') quotation marks
   * @throws NullPointerException if {@code string} is {@code null}
   */
  public static boolean stringContainsDoubleAndSingleQuotes(String string) {
    Preconditions.checkNotNull(string, "string was null");

    // test if string is already single or double quoted
    if (string.matches("^(['\"])((?!\\1).)*\\1$")) {
      return false;
    }

    return string.contains("\"") && string.contains("'");
  }

  /**
   * Indicates if a string is quoted (that is, surrounded by matching pairs
   * of either double or single quotes and does not contain the quote
   * character inside the outer quotes).
   *
   * <p><b>Examples</b></p>
   *
   * <p><ul>
   * <li>['Single quoted strings are "common" in JavaScript.'] - returns
   * {@code true}</li>
   * <li>["Howdy y'all!"] - returns {@code true}</li>
   * <li>["She said, "Make my day.""] - returns {@code false}</li>
   * </ul></p>
   *
   * @param string the string to check
   * @return {@code true} if {@code string} is either double or single quoted
   * @throws NullPointerException if {@code string} is {@code null}
   */
  public static boolean stringIsQuoted(String string) {
    Preconditions.checkNotNull(string, "string was null");

    return string.matches("^(['\"])((?!\\1).)*\\1$");
  }

  /**
   * Adds quotes around a string. If the string contains either no quotation
   * marks or single quotation marks, then the string is double quoted. If
   * the string contains double quote marks, then the string is single quoted.
   * An {@link AssertionError} is thrown if the string contains both double
   * and single quotes.
   *
   * <p><i>Note:</i> if a parameter is already quoted, additional quotation
   * marks are not added.</p>
   *
   * @param string the string to quote
   * @return the quoted string or the empty string if {@code string} is empty
   * @throws NullPointerException if {@code string} is {@code null}
   * @throws AssertionError if {@code string} contains both double and single
   *     quotes
   */
  public static String quoteString(String string) {
    Preconditions.checkNotNull(string, "string was null");

    String quoted = "";

    if(string.trim().length() > 0) {
      if (stringIsQuoted(string)) {
        quoted = string;
      } else {
        if (stringContainsDoubleAndSingleQuotes(string)) {
          throw new AssertionError("string: [" + string + "] contains both "
              + "double and single quotes");
        }

        if (string.contains("\"")) {
          quoted = "'" + string + "'";
        } else {
          quoted = "\"" + string + "\"";
        }
      }
    }
    return quoted;
  }

  /**
   * Quotes a string using {@link #quoteString(String)} if the string
   * contains at least one whitespace character as defined by the Java
   * regular expression character class "\\s" which equals [ \t\n\x0B\f\r].
   *
   * @param string the string to quote
   * @return the quoted string if it contains at least one whitespace
   *     character or the emtpy string if {@code string} is empty
   * @throws NullPointerException if {@code string} is {@code null}
   * @throws AssertionError if {@code string} contains both double and single
   *     quotes
   */
  public static String quoteStringIfContainsWhitespace(String string) {
    Preconditions.checkNotNull(string, "string was null");

    if (string.trim().matches(".*\\s+.*")) {
      return quoteString(string);
    }
    return string;
  }

  /**
   * Split a string into tokens delimited by whitespace, but treat quoted
   * strings as single tokens. Non-whitespace characters adjacent to quoted
   * strings will be returned as part of token. For example, the string
   * {@code "--js='/home/my project/app.js'"} would be returned as a single
   * token.
   *
   * @param data data to tokenize
   * @return a list of tokens
   */
  public static List<String> tokenizeKeepingQuotedStrings(String data) {
    List<String> tokens = Lists.newArrayList();
    Pattern tokenPattern =
        Pattern.compile("(?:[^ \t\f\\x0B'\"]|(?:'[^']*'|\"[^\"]*\"))+");
    // Windows: \r\n Unix: \n Mac: \r
    String[] lines = data.split("\\r?\\n|\\r");

    for (String line : lines) {
      Matcher matcher = tokenPattern.matcher(line);
      while (matcher.find()) {
        tokens.add(matcher.group(0));
      }
    }
    return tokens;
  }

  /**
   * Insert platform-specific line breaks every {@code charLineLength}
   * characters and return new string. Words are not split.
   *
   * @param string the string into which line breaks are inserted
   * @param charLineLength the number of characters per line
   * @return string with line breaks at least every {@code charLineLength}
   *     characters
   */
  public static String insertLineBreaks(String string, int charLineLength) {
    Preconditions.checkNotNull(string, "string is null");
    if (string.isEmpty()) {
      return string;
    }

    // Strip any existing line breaks; Windows: \r\n Unix: \n Mac: \r
    String[] lines = string.split("\\r?\\n|\\r");
    String noLineBreaks = Joiner.on("").join(lines);

    StringBuilder builder = new StringBuilder();

    Pattern linePattern =
        Pattern.compile(".{1, " + charLineLength + "}\\b");

    Matcher matcher = linePattern.matcher(noLineBreaks);
    while (matcher.find()) {
      builder.append(matcher.group(0)).append(String.format("%n"));
    }

    return builder.toString();
  }
}