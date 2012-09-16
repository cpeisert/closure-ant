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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.closureant.util.ClosureBuildUtil;

/**
 * Static factory class to create new instances of {@link JsClosureSourceFile}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class SourceFileFactory {
  private SourceFileFactory() {}

  /**
   * Constructs a {@link JsClosureSourceFile} for the given file. Namespaces
   * that are {@code goog.provided} and {@code goog.required} are parsed at the
   * time of construction.
   *
   *
   * @param file the underlying source file
   * @return a new {@link JsClosureSourceFile} instance
   * @throws IOException if file cannot be read
   * @throws NullPointerException if {@code file} is {@code null}
   */
  public static JsClosureSourceFile newJsClosureSourceFile(File file)
      throws IOException {
    return new BasicJsClosureSourceFile(file);
  }

  /**
   * Constructs a {@link JsClosureSourceFile} for a file specified by the given
   * name and source code contents. There need not be an actual operating
   * system file corresponding to the given name. Namespaces that are {@code
   * goog.provided} and {@code goog.required} are parsed at the time of
   * construction.
   *
   *
   * @param name the source file name
   * @param code the source code
   * @return a new {@link JsClosureSourceFile} instance
   * @throws NullPointerException if {@code file} is {@code null}
   */
  public static JsClosureSourceFile newJsClosureSourceFile(
      String name, String code) {
    return new BasicJsClosureSourceFile(name, code);
  }


  //----------------------------------------------------------------------------


  /**
   * Basic implementation of {@link JsClosureSourceFile}.
   */
  private static class BasicJsClosureSourceFile implements JsClosureSourceFile {

    private static final Pattern GOOG_PROVIDE =
        Pattern.compile("^\\s*goog\\.provide\\(\\s*['\"](.+)['\"]\\s*\\)");
    private static final Pattern GOOG_REQUIRE =
        Pattern.compile("^\\s*goog\\.require\\(\\s*['\"](.+)['\"]\\s*\\)");

    protected final File inputFile;
    protected final String fileName;
    protected final String sourceCode;
    protected final Set<String> provides;
    protected final Set<String> requires;
    protected boolean isClosureBaseJs;

    /**
     * Constructs a {@link JsClosureSourceFile} for the given file. Namespaces
     * that are {@code goog.provided} and {@code goog.required} are parsed at
     * the time of construction.
     *
     * @param file the underlying source file
     * @throws IllegalStateException if the underlying source file is Closure's
     *     base.js and it {@code goog.provides} or {@code goog.requires} one
     *     or more namespaces
     * @throws IOException if unable to read {@code file}
     * @throws NullPointerException if {@code file} is {@code null}
     */
    public BasicJsClosureSourceFile(File file) throws IOException {
      Preconditions.checkNotNull(file, "file was null");
      this.fileName = file.getName();
      this.sourceCode = Files.toString(file, Charsets.UTF_8);
      this.provides = Sets.newTreeSet();
      this.requires = Sets.newTreeSet();
      this.inputFile = file;
      this.scanInputFile();
    }

    /**
     * Constructs a {@link JsClosureSourceFile} for the given file. Namespaces
     * that are {@code goog.provided} and {@code goog.required} are parsed at
     * the time of construction.
     *
     * @param name the source file name
     * @param code the source code
     * @throws IllegalStateException if the underlying source file is Closure's
     *     base.js and it {@code goog.provides} or {@code goog.requires} one
     *     or more namespaces
     * @throws NullPointerException if {@code file} is {@code null}
     */
    public BasicJsClosureSourceFile(String name, String code) {
      Preconditions.checkNotNull(name, "name was null");
      Preconditions.checkNotNull(code, "code was null");
      this.fileName = name;
      this.sourceCode = code;
      this.provides = Sets.newTreeSet();
      this.requires = Sets.newTreeSet();
      this.inputFile = null;
      this.scanInputFile();
    }

    /**
     * Constructs a {@link JsClosureSourceFile} for the given file. Namespaces
     * that are {@code goog.provided} and {@code goog.required} are parsed at
     * the time of construction.
     *
     * @param file the underlying source file
     * @param code the source code
     * @throws IllegalStateException if the underlying source file is Closure's
     *     base.js and it {@code goog.provides} or {@code goog.requires} one
     *     or more namespaces
     * @throws NullPointerException if {@code file} is {@code null}
     */
    public BasicJsClosureSourceFile(File file, String code) {
      Preconditions.checkNotNull(file, "file was null");
      Preconditions.checkNotNull(code, "code was null");
      this.fileName = file.getName();
      this.sourceCode = code;
      this.provides = Sets.newTreeSet();
      this.requires = Sets.newTreeSet();
      this.inputFile = null;
      this.scanInputFile();
    }

    /**
     * Copy constructor for subclasses.
     *
     * @param jsSourceFile JsClosureSourceFile to copy
     * @throws NullPointerException if {@code jsSourceFile} is {@code null}
     */
    protected BasicJsClosureSourceFile(BasicJsClosureSourceFile jsSourceFile) {
      Preconditions.checkNotNull(jsSourceFile, "jsSourceFile was null");
      this.fileName = jsSourceFile.fileName;
      this.sourceCode = jsSourceFile.sourceCode;
      this.provides = jsSourceFile.provides;
      this.requires = jsSourceFile.requires;
      this.inputFile = jsSourceFile.inputFile;
      this.isClosureBaseJs = jsSourceFile.isClosureBaseJs;
    }

    public String getAbsolutePath() {
      return (this.inputFile != null) ? this.inputFile.getAbsolutePath() : "";
    }

    /**
     * The JavaScript source code.
     *
     * @return the source code
     */
    public String getCode() {
      return this.sourceCode;
    }

    public String getName() {
      return this.fileName;
    }

    public Collection<String> getProvides() {
      return ImmutableSet.copyOf(this.provides);
    }

    public Collection<String> getRequires() {
      return ImmutableSet.copyOf(this.requires);
    }

    public boolean isBaseJs() {
      return this.isClosureBaseJs;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof BasicJsClosureSourceFile)) {
        return false;
      }

      BasicJsClosureSourceFile that = (BasicJsClosureSourceFile) o;

      if (isClosureBaseJs != that.isClosureBaseJs) {
        return false;
      }
      if (!fileName.equals(that.fileName)) {
        return false;
      }
      if (!provides.equals(that.provides)) {
        return false;
      }
      if (!requires.equals(that.requires)) {
        return false;
      }
      if (!sourceCode.equals(that.sourceCode)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = fileName.hashCode();
      result = 31 * result + sourceCode.hashCode();
      result = 31 * result + provides.hashCode();
      result = 31 * result + requires.hashCode();
      result = 31 * result + (isClosureBaseJs ? 1 : 0);
      return result;
    }

    /**
     * Returns the file name.
     *
     * @return the string form of this JavaScript source file
     */
    @Override
    public String toString() {
      return this.fileName;
    }

    /**
     * Extracts the {@code goog.provided} and {@code goog.required} namespaces
     * from the source code. Determines if this is Closure Library's base.js.
     *
     * @throws IllegalStateException if the underlying source file is Closure's
     * base.js and it {@code goog.provides} or {@code goog.requires} one or
     * more namespaces
     */
    private void scanInputFile() {
      // Windows: \r\n Unix: \n Mac: \r
      String[] lines = this.sourceCode.split("\\r?\\n|\\r");

      for (String line : lines) {
        Matcher matcher = GOOG_REQUIRE.matcher(line);
        if (matcher.find()) {
          this.requires.add(matcher.group(1));
        } else {
          matcher = GOOG_PROVIDE.matcher(line);
          if (matcher.find()) {
            this.provides.add(matcher.group(1));
          }
        }
      }

      if ("base.js".equalsIgnoreCase(fileName)) {
        if (ClosureBuildUtil.isClosureBaseJs(sourceCode)) {
          this.isClosureBaseJs = true;
          if (!this.provides.isEmpty() || !this.requires.isEmpty()) {
            throw new IllegalStateException("base.js should not provide or "
                + "require namespaces");
          }
          // The Closure Library's base.js file implicitly provides "goog".
          provides.add("goog");
        }
      }
    }
  }
}