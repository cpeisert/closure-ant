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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.closureant.util.StringUtil}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class StringUtilTest {

  @Test(expected = NullPointerException.class)
  public void stringContainsDoubleAndSingleQuotes_NullString() {
    StringUtil.stringContainsDoubleAndSingleQuotes(null);
  }

  @Test public void
  stringContainsDoubleAndSingleQuotes_DoubleAndSingleQuotes() {
    assertTrue(StringUtil.stringContainsDoubleAndSingleQuotes(
        "Isn't it \"grand\"?"));
  }

  @Test public void stringContainsDoubleAndSingleQuotes_DoubleQuotedString() {
    assertFalse(StringUtil.stringContainsDoubleAndSingleQuotes(
        "\"Howdy y'all!\""));
  }

  @Test public void
  stringContainsDoubleAndSingleQuotes_DoubleQuotesWithinDoubleQuotedString() {
    assertFalse(StringUtil.stringContainsDoubleAndSingleQuotes(
        "\"She said, \"Make my day.\"\""));
  }

  @Test(expected = NullPointerException.class)
  public void stringIsQuoted_NullString() {
    StringUtil.stringIsQuoted(null);
  }

  @Test public void stringIsQuoted_SingleQuoted() {
    assertTrue(StringUtil.stringIsQuoted("'Single quoted strings are " 
        + "\"common\" in JavaScript.'"));
  }

  @Test public void stringIsQuoted_DoubleQuoted() {
    assertTrue(StringUtil.stringIsQuoted("\"Howdy y'all!\""));
  }

  @Test public void stringIsQuoted_DoubleQuotedWithinDoubleQuoted() {
    assertFalse(StringUtil.stringIsQuoted("\"She said, \"Make my day.\"\""));
  }

  @Test(expected = NullPointerException.class)
  public void quoteString_NullString() {
    StringUtil.quoteString(null);
  }

  @Test(expected = AssertionError.class)
  public void quoteString_DoubleAndSingleQuotes() {
    StringUtil.quoteString("Double and single \"quotes\" aren't good together");
  }

  @Test public void quoteString_NoQuotes() {
    assertEquals("\"simple string\"", StringUtil.quoteString("simple string"));
  }

  @Test public void quoteString_SingleQuote() {
    assertEquals("\"I've got a single quote.\"",
        StringUtil.quoteString("I've got a single quote."));
  }

  @Test public void quoteString_DoubleQuotes() {
    assertEquals("'Sometimes the answer is \"no\".'",
        StringUtil.quoteString("Sometimes the answer is \"no\"."));
  }

  @Test public void quoteStringIfContainsWhitespace_HasWhitespace() {
    assertEquals("\"contains whitespace\"",
        StringUtil.quoteStringIfContainsWhitespace("contains whitespace"));
  }

  @Test public void quoteStringIfContainsWhitespace_NoWhitespace() {
    assertEquals("no/whitespace/here",
        StringUtil.quoteStringIfContainsWhitespace("no/whitespace/here"));
  }

  @Test public void tokenizeKeepingQuotedStrings() {
    String data = "t1 t2 t3='quoted string' --t4=\"/my project/app.js\" "
        + String.format("%n") + "'adjacent content't5 t6='single' \"t7\"";
    List<String> tokens = StringUtil.tokenizeKeepingQuotedStrings(data);

    List<String> expected = ImmutableList.of("t1", "t2", "t3='quoted string'",
        "--t4=\"/my project/app.js\"", "'adjacent content't5", "t6='single'",
        "\"t7\"");
    assertEquals(expected, tokens);
  }
}