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

package org.closureant.css;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;
import com.google.gson.JsonParseException;
import com.google.template.soy.shared.SoyCssRenamingMap;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.closureant.util.GsonUtil;

/**
 * An implementation of {@link SoyCssRenamingMap} built on top of Guava's
 * bidirectional map: {@link com.google.common.collect.BiMap}.
 *
 * <p><b>Note:</b> the {@link SoyCssRenamingMap} interface specifies that:
 * <i>"The same value must be consistently returned for any particular key,
 * and the returned value must not be returned for any other key value."</i>
 * Using a bidirectional map ensures these constraints are satisfied.</p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class CssRenamingMap
    implements SoyCssRenamingMap, Map<String, String> {

  private final BiMap<String, String> cssRenamingMap;

  /**
   * Creates a new, empty {@link CssRenamingMap} with the default initial
   * capacity (16).
   *
   * @return a new {@link CssRenamingMap}
   */
  public static CssRenamingMap create() {
    return new CssRenamingMap();
  }

  /**
   * Creates a new, empty {@link CssRenamingMap} with the specified
   * expected size.
   *
   * @param expectedSize the expected number of entries
   * @return a new {@link CssRenamingMap}
   * @throws IllegalArgumentException if the specified expected size is negative
   */
  public static CssRenamingMap createWithExpectedSize(int expectedSize) {
    return new CssRenamingMap(expectedSize);
  }

  /**
   * Creates a new {@link CssRenamingMap} containing initial values from
   * map. The {@link CssRenamingMap} is created with an initial capacity
   * sufficient to hold the mappings in the specified map.
   *
   * @param map the map containing initial values
   * @return a new {@link CssRenamingMap}
   */
  public static CssRenamingMap createFromMap(Map<String, String> map) {
    return new CssRenamingMap(map);
  }

  /**
   * Creates a new {@link CssRenamingMap} populated from the JSON in
   * the specified file. The JSON object must have keys and values of type
   * string (i.e. no numbers, boolean values, nested arrays, or objects).
   *
   * <p>Any characters outside the outermost matching set of curly braces
   * are ignored. This flexibility means that all of the CSS renaming map
   * output formats supported by <a target="_blank"
   * href="http://code.google.com/p/closure-stylesheets/">Closure Stylesheets
   * </a> (with the exception of Java {@link Properties}) may be passed
   * directly to this method (JSON, CLOSURE_COMPILED, and CLOSURE_UNCOMPILED).
   * </p>
   *
   * @param jsonFile file containing a JSON object representing a CSS renaming
   *     map
   * @return a new {@link CssRenamingMap}
   * @throws ClassCastException if JSON object contains non-string values such
   *     as number or boolean
   * @throws java.io.IOException if there is an error reading the file
   * @throws JsonParseException if the file does not contain valid JSON
   */
  public static CssRenamingMap createFromJsonFile(File jsonFile)
      throws IOException, JsonParseException {
    String fileContents = Files.toString(jsonFile, Charsets.UTF_8);
    return new CssRenamingMap(
        internalCreateBiMapFromJson(fileContents));
  }

  /**
   * Creates a new {@link CssRenamingMap} populated from the specified Java
   * {@link Properties} file.
   *
   * @param propertiesFile a Java {@link Properties} file
   * @return a new {@link CssRenamingMap}
   * @throws java.io.IOException if there is an error reading the file
   */
  public static CssRenamingMap createFromJavaPropertiesFile(File propertiesFile)
      throws IOException {
    String fileContents = Files.toString(propertiesFile, Charsets.UTF_8);
    return new CssRenamingMap(
        internalCreateBiMapFromJavaProperties(fileContents));
  }

  /** Private constructor. Use static factory method {@link #create()}. */
  private CssRenamingMap() {
    this.cssRenamingMap = HashBiMap.create();
  }

  /**
   * Private constructor. Use static factory method {@link
   * #createWithExpectedSize(int)}.
   */
  private CssRenamingMap(int expectedSize) {
    this.cssRenamingMap = HashBiMap.create(expectedSize);
  }

  /**
   * Private constructor. Use static factory method {@link
   * #createFromMap(java.util.Map)}.
   */
  private CssRenamingMap(Map<String, String> map) {
    this.cssRenamingMap = HashBiMap.create(map);
  }

  /**
   * Creates a new {@link BiMap} from a JSON string. The JSON must be an simple
   * object with keys and values of type string (i.e. no numbers, boolean
   * values, nested arrays, or objects). Any characters outside the JSON object
   * (i.e. preceding the opening curly brace or following the closing curly
   * brace) will be ignored.
   *
   * <p>Note: this method is package private instead of private for testing.</p>
   *
   * @param json the JSON to parse
   * @return a {@link BiMap} populated with key-value pairs from the JSON
   * @throws ClassCastException if JSON object contains non-string values such
   *     as number or boolean
   * @throws JsonParseException if the specified text is not valid JSON
   */
  static BiMap<String, String> internalCreateBiMapFromJson(String json)
      throws JsonParseException {

    Pattern jsonObjectPattern =
        Pattern.compile("(?s)^[^\\{]*(\\{.*\\})[^\\}]*$");

    Matcher matcher = jsonObjectPattern.matcher(json);
    if (matcher.matches()) {
      json = matcher.group(1);
    }

    Map<String, Object> jsonMap = GsonUtil.parseJsonToMap(json);
    BiMap<String, String> map = HashBiMap.create();

    for (Entry<String, Object> keyValuePair : jsonMap.entrySet()) {
      map.put(keyValuePair.getKey(), (String)keyValuePair.getValue());
    }

    return map;
  }

  /**
   * Creates a new {@link BiMap} from a string of Java {@link Properties}
   * (key-value pairs separated by equals '=' or colon ':').
   *
   * <p>Note: this method is package private instead of private for testing.</p>
   *
   * @param javaProperties string of Java properties
   * @return a {@link BiMap} populated with key-value pairs
   * @throws IOException if an error occurred when reading from the input
   *     stream
   */
  static BiMap<String, String> internalCreateBiMapFromJavaProperties(
      String javaProperties) throws IOException {
    BiMap<String, String> map = HashBiMap.create();

    Properties cssProperties = new Properties();
    cssProperties.load(new StringReader(javaProperties));

    for (Entry<Object, Object> property : cssProperties.entrySet()) {
      map.put((String)(property.getKey()), (String)(property.getValue()));
    }
    return map;
  }


  // Query Operations

  /** {@inheritDoc} */
  public int size() {
    return this.cssRenamingMap.size();
  }

  /** {@inheritDoc} */
  public boolean isEmpty() {
    return this.cssRenamingMap.isEmpty();
  }

  /** {@inheritDoc} */
  public boolean containsKey(Object key) {
    return this.cssRenamingMap.containsKey(key);
  }

  /** {@inheritDoc} */
  public boolean containsValue(Object value) {
    return this.cssRenamingMap.containsValue(value);
  }

  /**
   * Gets the string that should be substituted for key. The same value must
   * be consistently returned for any particular key, and the returned value
   * must not be returned for any other key value.
   *
   * @param key the CSS text to be replaced
   * @return the value to substitute for {@code key}
   */
  public String get(String key) {
    return cssRenamingMap.get(key);
  }

  /**
   * This method exists to make the Java compiler happy that the {@link java.util.Map}
   * interface is implemented. See {@link #get(String)}.
   */
  public String get(Object key) {
    return cssRenamingMap.get(key);
  }


  // Modification Operations

  /**
   * An alternate form of {@code put} that silently removes any existing entry
   * with the value {@code value} before proceeding with the {@link #put}
   * operation. If the {@link CssRenamingMap} previously contained the
   * provided key-value mapping, this method has no effect.
   *
   * <p>Note that a successful call to this method could cause the size of the
   * {@link CssRenamingMap} to increase by one, stay the same, or even decrease
   * by one.</p>
   *
   * <p><b>Warning:</b> If an existing entry with this value is removed, the
   * key for that entry is discarded and not returned.</p>
   *
   * @param key the key with which the specified value is to be associated
   * @param value the value to be associated with the specified key
   * @return the value which was previously associated with the key, which may
   *     be {@code null}, or {@code null} if there was no previous entry
   */
  public String forcePut(String key, String value) {
    return this.cssRenamingMap.forcePut(key, value);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if the given value is already bound to a
   *     different key in this {@link CssRenamingMap}. The {@link
   *     CssRenamingMap} will remain unmodified in this event. To avoid this
   *     exception, call {@link #forcePut} instead.
   */
  public String put(String key, String value) {
    return this.cssRenamingMap.put(key, value);
  }

  /** {@inheritDoc} */
  public String remove(Object key) {
    return this.cssRenamingMap.remove(key);
  }


  // Bulk Operations

  /**
   * {@inheritDoc}
   *
   * <p><b>Warning:</b> the results of calling this method may vary depending
   * on the iteration order of {@code map}.</p>
   *
   * @throws IllegalArgumentException if an attempt to {@code put} any
   *     entry fails. Note that some map entries may have been added to the
   *     {@link CssRenamingMap} before the exception was thrown.
   */
  public void putAll(Map<? extends String, ? extends String> map) {
    this.cssRenamingMap.putAll(map);
  }

  /** {@inheritDoc} */
  public void clear() {
    this.cssRenamingMap.clear();
  }


  // Views

  /** {@inheritDoc} */
  public Set<String> keySet() {
    return this.cssRenamingMap.keySet();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Because a {@link CssRenamingMap} has unique values, this method
   * returns a {@link java.util.Set}, instead of the {@link java.util.Collection}
   * specified in the {@link java.util.Map} interface.</p>
   */
  public Set<String> values() {
    return this.cssRenamingMap.values();
  }

  /** {@inheritDoc} */
  public Set<Entry<String, String>> entrySet() {
    return this.cssRenamingMap.entrySet();
  }

  @Override
  public String toString() {
    return this.cssRenamingMap.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CssRenamingMap that = (CssRenamingMap) o;

    if (!cssRenamingMap.equals(that.cssRenamingMap)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return cssRenamingMap.hashCode();
  }
}
