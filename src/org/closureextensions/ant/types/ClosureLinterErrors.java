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

package org.closureextensions.ant.types;

import org.closureextensions.ant.CommandLineBuilder;

/**
 * Ant data type to set error flags for Closure Linter.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ClosureLinterErrors {

  private Boolean all;

  // Corresponds to flag combination: --jslint_error blank_lines_at_top_level
  private Boolean blankLinesAtTopLevel;

  // Corresponds to flag combination: --jslint_error braces_around_type
  private Boolean bracesAroundType;

  // Corresponds to flag combination: --jslint_error indentation
  private Boolean indentation;

  // Corresponds to flag --jsdoc defined in errorrules.py.
  private Boolean missingJsDoc;

  // Corresponds to flag combination: --jslint_error no_braces_around_inherit_doc
  private Boolean noBracesAroundInheritDoc;

  // Corresponds to flag combination: --jslint_error optional_type_marker
  private Boolean optionalTypeMarker;

  // Corresponds to flag --strict defined in error_check.py.
  private Boolean strict;

  // Corresponds to flag combination: --jslint_error unused_private_members
  private Boolean unusedPrivateMembers;

  // Corresponds to flag combination: --jslint_error well_formed_author
  private Boolean wellFormedAuthor;


  public ClosureLinterErrors() {
    this.all = null;
    this.blankLinesAtTopLevel = null;
    this.bracesAroundType = null;
    this.indentation = null;
    this.missingJsDoc = null;
    this.noBracesAroundInheritDoc = null;
    this.optionalTypeMarker = null;
    this.strict = null;
    this.unusedPrivateMembers = null;
    this.wellFormedAuthor = null;
  }

  /**
   * Enables all Closure Linter error checks.
   *
   * @param all whether or not to enable all Closure Linter error checks
   */
  public void setAll(boolean all) {
    this.all = all;
  }

  /** @return {@code true} if all Closure Linter error checks enabled */
  public Boolean getAll() {
    return this.all;
  }

  /**
   * Validates the number of blank lines between top-level blocks.
   *
   * <p><ul>
   * <li>constructor/interface - should be preceded by three blank lines</li>
   * <li>file overview - should be preceded by one blank line</li>
   * <li>other top-level blocks - should be preceded by two blank lines</li>
   * </ul></p>
   *
   * @param blankLinesAtTopLevel {@code true} to check blank lines between
   *     top-level blocks
   */
  public void setBlankLinesAtTopLevel(boolean blankLinesAtTopLevel) {
    this.blankLinesAtTopLevel = blankLinesAtTopLevel;
  }

  /** @return {@code true} if blank line validation enabled */
  public Boolean getBlankLinesAtTopLevel() {
    return this.blankLinesAtTopLevel;
  }

  /**
   * Enforces braces around types in JsDoc tags.
   *
   * @param bracesAroundType {@code true} to check braces around types in
   *     JsDoc tags
   */
  public void setBracesAroundType(boolean bracesAroundType) {
    this.bracesAroundType = bracesAroundType;
  }

  /** @return {@code true} if braces around types in JsDoc tags enforced */
  public Boolean getBracesAroundType() {
    return this.bracesAroundType;
  }

  /**
   * Checks correct indentation of code.
   *
   * @param indentation {@code true} to check indentation of code
   */
  public void setIndentation(boolean indentation) {
    this.indentation = indentation;
  }

  /** @return {@code true} if indentation checking enabled */
  public Boolean getIndentation() {
    return this.indentation;
  }

  /**
   * Whether to report errors for missing JsDoc. Even with this flag enabled,
   * existing JsDoc annotations are still verified for correctness.
   *
   * @param missingJsDoc {@code true} to check for missing JsDoc
   */
  public void setMissingJsDoc(boolean missingJsDoc) {
    this.missingJsDoc = missingJsDoc;
  }

  /** @return {@code true} if errors are reported for missing JsDoc */
  public Boolean getMissingJsDoc() {
    return this.missingJsDoc;
  }

  /**
   * Forbids braces around {@code @inheritdoc} JsDoc tags.
   *
   * @param noBracesAroundInheritDoc {@code true} to forbid braces around
   *     {@code @inheritdoc} JsDoc tag
   */
  public void setNoBracesAroundInheritDoc(boolean noBracesAroundInheritDoc) {
    this.noBracesAroundInheritDoc = noBracesAroundInheritDoc;
  }

  /**
   * @return {@code true} if braces around {@code @inheritdoc} JsDoc tag are
   *     forbidden
   */
  public Boolean getNoBracesAroundInheritDoc() {
    return this.noBracesAroundInheritDoc;
  }

  /**
   * Checks correct use of optional parameter marker {@code =} in
   * {@code @param} types.
   *
   * @param optionalTypeMarker {@code true} to check for correct use of
   *     optional parameter marker {@code =}
   */
  public void setOptionalTypeMarker(boolean optionalTypeMarker) {
    this.optionalTypeMarker = optionalTypeMarker;
  }

  /**
   * @return {@code true} if optional parameter marker {@code =} is used
   *     correctly
   */
  public Boolean getOptionalTypeMarker() {
    return this.optionalTypeMarker;
  }

  /**
   * Whether to validate against the stricter Closure style. Setting {@code
   * strict} is equivalent to enabling the following error checks:
   *
   * <p><ul>
   * <li>blankLinesAtTopLevel</li>
   * <li>bracesAroundType</li>
   * <li>indentation</li>
   * <li>noBracesAroundInheritDoc</li>
   * <li>optionalTypeMarker</li>
   * <li>wellFormedAuthor</li>
   * </ul></p>
   *
   * @param strict {@code true} to validate against the stricter Closure style
   */
  public void setStrict(boolean strict) {
    this.strict = strict;
  }

  /** @return {@code true} if validating against the stricter Closure style */
  public Boolean getStrict() {
    return this.strict;
  }

  /**
   * Checks for unused private variables.
   *
   * @param unusedPrivateMembers {@code true} to check for unused private
   *     variables
   */
  public void setUnusedPrivateMembers(boolean unusedPrivateMembers) {
    this.unusedPrivateMembers = unusedPrivateMembers;
  }

  /** @return {@code true} if checking for unused private variables */
  public Boolean getUnusedPrivateMembers() {
    return this.unusedPrivateMembers;
  }

  /**
   * Validates the {@code @author} JsDoc tags.
   *
   * @param wellFormedAuthor {@code true} to validate the {@code @author} JsDoc
   *     tags
   */
  public void setWellFormedAuthor(boolean wellFormedAuthor) {
    this.wellFormedAuthor = wellFormedAuthor;
  }

  /** @return {@code true} if validating the {@code @author} JsDoc tag */
  public Boolean getWellFormedAuthor() {
    return this.wellFormedAuthor;
  }

  /**
   * Gets a command line suitable for {@code gjslint} based on the error flags
   * set in this Ant data type.
   *
   * @return a command line suitable for {@code gjslint}
   */
  public CommandLineBuilder getCommandLineForErrorFlags() {
    CommandLineBuilder cmdline = new CommandLineBuilder();

    if (Boolean.TRUE.equals(this.all)) {
      cmdline.argument("--all");
      cmdline.argument("--jsdoc");
    } else if (Boolean.FALSE.equals(this.all)) {
      cmdline.argument("--noall");
      cmdline.argument("--nojsdoc");
    }

    if (Boolean.TRUE.equals(this.strict)) {
      cmdline.argument("--strict");
    } else if (Boolean.FALSE.equals(this.strict)) {
      cmdline.argument("--nostrict");
    }

    if (Boolean.TRUE.equals(this.blankLinesAtTopLevel)) {
      cmdline.flagAndArgument("--jslint_error", "blank_lines_at_top_level");
    } else if (Boolean.FALSE.equals(this.blankLinesAtTopLevel)) {
      cmdline.flagAndArgument("--jslint_noerror", "blank_lines_at_top_level");
    }

    if (Boolean.TRUE.equals(this.bracesAroundType)) {
      cmdline.flagAndArgument("--jslint_error", "braces_around_type");
    } else if (Boolean.FALSE.equals(this.bracesAroundType)) {
      cmdline.flagAndArgument("--jslint_noerror", "braces_around_type");
    }

    if (Boolean.TRUE.equals(this.indentation)) {
      cmdline.flagAndArgument("--jslint_error", "indentation");
    } else if (Boolean.FALSE.equals(this.indentation)) {
      cmdline.flagAndArgument("--jslint_noerror", "indentation");
    }

    if (Boolean.TRUE.equals(this.missingJsDoc)) {
      cmdline.argument("--jsdoc");
    } else if (Boolean.FALSE.equals(this.missingJsDoc)) {
      cmdline.argument("--nojsdoc");
    }

    if (Boolean.TRUE.equals(this.noBracesAroundInheritDoc)) {
      cmdline.flagAndArgument("--jslint_error", "no_braces_around_inherit_doc");
    } else if (Boolean.FALSE.equals(this.noBracesAroundInheritDoc)) {
      cmdline.flagAndArgument("--jslint_noerror",
          "no_braces_around_inherit_doc");
    }

    if (Boolean.TRUE.equals(this.optionalTypeMarker)) {
      cmdline.flagAndArgument("--jslint_error", "optional_type_marker");
    } else if (Boolean.FALSE.equals(this.optionalTypeMarker)) {
      cmdline.flagAndArgument("--jslint_noerror", "optional_type_marker");
    }

    if (Boolean.TRUE.equals(this.unusedPrivateMembers)) {
      cmdline.flagAndArgument("--jslint_error", "unused_private_members");
    } else if (Boolean.FALSE.equals(this.unusedPrivateMembers)) {
      cmdline.flagAndArgument("--jslint_noerror", "unused_private_members");
    }

    if (Boolean.TRUE.equals(this.wellFormedAuthor)) {
      cmdline.flagAndArgument("--jslint_error", "well_formed_author");
    } else if (Boolean.FALSE.equals(this.wellFormedAuthor)) {
      cmdline.flagAndArgument("--jslint_noerror", "well_formed_author");
    }

    return cmdline;
  }
}
