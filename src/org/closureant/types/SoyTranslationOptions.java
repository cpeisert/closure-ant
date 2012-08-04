/*
 * Copyright 2008 Google Inc.
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

package org.closureant.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import org.closureant.util.AntUtil;
import org.closureant.types.LocaleList;

import java.util.List;
import java.util.Set;

/**
 * Ant type for setting options related to Soy template translation activities
 * such as extracting messages from Soy templates and specifying xliff files
 * to use for localized rendering and JavaScript compilation.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class SoyTranslationOptions {

  // Attributes
  private String extractedMessagesOutputFile;
  private Path messagePluginClasspath;
  private String messagePluginModule;
  private String extractedMessagesSourceLocale;

  // Nested elements
  private final Set<String> extractedMessagesTargetLocales;
  private final List<FileSet> translationFileSets;

  /**
   * Constructs a new Ant type to store Soy translation related preferences.
   */
  public SoyTranslationOptions() {
    this.extractedMessagesOutputFile = null;
    this.messagePluginClasspath = null;
    this.messagePluginModule = null;
    this.extractedMessagesSourceLocale = null;

    this.extractedMessagesTargetLocales = Sets.newHashSet();
    this.translationFileSets = Lists.newArrayList();
  }

  // Attributes

  /**
   * A Java classpath to search for message plugin modules. See {@link Path}.
   *
   * @param messagePluginClasspath a Java classpath
   */
  public void setMessagePluginClasspath(Path messagePluginClasspath) {
    if (this.messagePluginClasspath == null) {
      this.messagePluginClasspath =
          new Path(messagePluginClasspath.getProject());
    }
    this.messagePluginClasspath.append(messagePluginClasspath);
  }

  /**
   * @return the message plugin module classpath
   */
  public Path getMessagePluginClasspath() {
    return this.messagePluginClasspath;
  }

  /**
   * Adds a reference to a classpath defined elsewhere to use when searching
   * for Soy message plugin modules. See {@link Reference}.
   *
   * @param ref a reference to a classpath defined elsewhere (for example,
   *     in a {@literal <classpath>} element)
   */
  public void setMessagePluginClasspathRef(Reference ref) {
    if (this.messagePluginClasspath == null) {
      this.messagePluginClasspath = new Path(ref.getProject());
    }
    this.messagePluginClasspath.setRefid(ref);
  }

  /**
   * Specifies the full class name of a Guice module that binds a
   * {@link com.google.template.soy.msgs.SoyMsgPlugin}. If not specified,
   * the default is {@link
   * com.google.template.soy.xliffmsgplugin.XliffMsgPluginModule}, which binds
   * the {@code XliffMsgPlugin}.
   *
   * @param messagePluginModule full class name of the Guice module for the
   *     {@code SoyMsgPlugin}
   */
  public void setMessagePluginModule(String messagePluginModule) {
    this.messagePluginModule = messagePluginModule;
  }

  /**
   * @return the full class name of the Guice message plugin module
   */
  public String getMessagePluginModule() {
    return this.messagePluginModule;
  }

  /**
   * The locale string of the source language (default "en") to use when
   * creating a file of extracted messages.
   *
   * @param sourceLocale locale string of the source language. Defaults
   *     to "en".
   */
  public void setExtractedMessagesSourceLocale(String sourceLocale) {
    this.extractedMessagesSourceLocale = sourceLocale;
  }

  /**
   * @return the locale string of the source language to use when creating a
   *     file of extracted messages
   */
  public String getExtractedMessagesSourceLocale() {
    return this.extractedMessagesSourceLocale;
  }

  /**
   * The locale string of a target language (default empty) to use when
   * creating a file of extracted messages. If empty, then the output
   * messages file will not specify a target locale string. Note that this
   * option may not be applicable for certain message plugins (in which case
   * this value will be ignored by the message plugin).
   *
   * @param targetLocale locale string of the target language. Defaults
   *     to empty.
   */
  public void setExtractedMessagesTargetLocale(String targetLocale) {
    this.extractedMessagesTargetLocales.add(targetLocale);
  }

  /**
   * @return locale strings of the target languages to use when creating files
   *     of extracted messages
   */
  public Set<String> getExtractedMessagesTargetLocales() {
    return this.extractedMessagesTargetLocales;
  }

  /**
   * The name of the output file to which extracted messages should be written
   * (for example, an XLIFF file with the ".xlf" file extension). The format
   * string can include literal characters as well as the placeholders {@code
   * {INPUT_FILE_NAME}}, {@code {INPUT_FILE_NAME_NO_EXT}},  {@code {LOCALE}},
   * {@code {LOCALE_LOWER_CASE}}.
   *
   * <p><b>Note:</b> {@code {LOCALE_LOWER_CASE}} turns dash into
   * underscore, e.g. {@code pt-BR} becomes {@code pt_br}.</p>
   *
   * @param extractedMessagesOutputFile the output translation file name
   */
  public void setExtractedMessagesOutputFile(
      String extractedMessagesOutputFile) {
    this.extractedMessagesOutputFile = extractedMessagesOutputFile;
  }

  /**
   * @return the extracted messages output file name (format string)
   */
  public String getExtractedMessagesOutputFile() {
    return this.extractedMessagesOutputFile;
  }


  // Nested elements

  /**
   * The locale string of a target language (default empty) to use when
   * creating a file of extracted messages. If empty, then the output
   * messages file will not specify a target locale string. Note that this
   * option may not be applicable for certain message plugins (in which case
   * this value will be ignored by the message plugin).
   *
   * @param localeList list of locale strings of the target languages to use
   *     when creating files of extracted messages. Defaults to empty.
   */
  public void addConfiguredExtractedMessagesTargetLocales(
      LocaleList localeList) {
    this.extractedMessagesTargetLocales.addAll(localeList.getLocales());
  }

  /**
   * Translation input files, where each file contains a full set of
   * translated messages for some language/locale. If a message plugin
   * module is not specified, the translation files are assumed to be in XLIFF
   * format.
   *
   * @param translationFiles an Ant {@link FileSet} containing translation
   *     input files
   */
  public void addTranslationFileSet(FileSet translationFiles) {
    this.translationFileSets.add(translationFiles);
  }

  /**
   * @param project the Ant project
   * @return a list of the translation input file paths
   */
  public List<String> getTranslationFilePaths(Project project) {
    return AntUtil.getFilePathsFromCollectionOfFileSet(project,
        this.translationFileSets);
  }
}