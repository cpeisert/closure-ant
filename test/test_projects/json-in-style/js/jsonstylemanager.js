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

/**
 * @fileoverview A JSON style manager to encapsulate the details of dynamically
 * setting the colors for the CSS classes used by
 * {@code goog.format.JsonPrettyPrinter}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */

goog.provide('jsonstyle.JsonStyle');
goog.provide('jsonstyle.JsonStyleManager');

goog.require('goog.dom');
goog.require('goog.dom.DomHelper');
goog.require('goog.format.JsonPrettyPrinter');
goog.require('goog.string');
goog.require('goog.style');


/**
 * Helper object to store information for a JSON style used by
 * {@code goog.format.JsonPrettyPrinter}.
 *
 * @param {string} cssClass The name of the CSS class for a JSON attribute such
 *     as a property name (for example, 'goog-jsonprettyprinter-propertyname').
 *     Note: if the class has a dot ('.') prefix, the dot will be removed.
 * @param {string=} opt_color Optional style color. Defaults to black.
 * @constructor
 */
jsonstyle.JsonStyle = function(cssClass, opt_color) {

  this.cssClass_ = goog.string.startsWith(cssClass, '.') ?
      goog.string.remove(cssClass, '.') : cssClass;
  this.color_ = goog.isDef(opt_color) ? opt_color : '#000000';
  this.installedStyleElement_ = undefined;
};

/**
 * @return {string} The name of the JSON style CSS class without a dot ('.')
 *     prefix.
 */
jsonstyle.JsonStyle.prototype.getCssClass = function() {
  return this.cssClass_;
};

/** @return {string} The style color. */
jsonstyle.JsonStyle.prototype.getColor = function() {
  return this.color_;
};

/** @param color The style color. */
jsonstyle.JsonStyle.prototype.setColor = function(color) {
  this.color_ = color;
};

/**
 * @return {Element|undefined} The HTML style element used to install this
 *     style.
 */
jsonstyle.JsonStyle.prototype.getInstalledStyleElement = function() {
  return this.installedStyleElement_;
};

/**
 * @param {!Element} element The HTML element into which this style was
 *     installed.
 */
jsonstyle.JsonStyle.prototype.setInstalledStyleElement = function(element) {
  this.installedStyleElement_ = element;
};

/**
 * @return {string} A CSS style string representing this object. For example,
 *     {@code .goog-jsonprettyprinter-propertyname {color: #000;}}.
 */
jsonstyle.JsonStyle.prototype.getCssString = function() {
  return '.' + this.cssClass_ + ' {color: ' + this.color_ + ';}';
};


//------------------------------------------------------------------------------


/**
 * Creates a {@code JsonStyleManager} for dynamically setting the colors of
 * the {@code goog.format.JsonPrettyPrinter} CSS classes. During construction,
 * the {@code goog.format.JsonPrettyPrinter} styles are installed into the
 * document specified by {@code opt_domHelper}. Hence, there is no need to
 * specify the styles in a stylesheet or HTML document. All of the JSON style
 * colors default to black unless {@code opt_colors} is specified.
 *
 * <p><b>Note:</b> since {@code goog.format.JsonPrettyPrinter} is does not use
 * {@code goog.getCssName()} to wrap CSS classes, code that references these
 * styles must likewise _not_ use {@code goog.getCssName()}.</p>
 *
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @param {Object.<string>=} opt_colors Optional object specifying default
 *     colors. One or more of the following object keys may be specified:
 *     {@code propertyName}, {@code stringValue}, {@code numberValue},
 *     {@code booleanValue}, {@code nullValue}. The colors default to black.
 *     <p><b>Note</b>: the object keys must be quoted to avoid renaming by the
 *     Compiler.</p>
 * @constructor
 */
jsonstyle.JsonStyleManager = function(opt_domHelper, opt_colors) {

  /**
   * DomHelper used to interact with the document, allowing components to be
   * created in a different window.
   * @type {!goog.dom.DomHelper}
   * @protected
   * @suppress {underscore}
   */
  this.dom_ = /** @type {!goog.dom.DomHelper} */
      (goog.isDef(opt_domHelper) ? opt_domHelper : goog.dom.getDomHelper());

  var propertyNameColor = '#000000';
  var stringValueColor = '#000000';
  var numberValueColor = '#000000';
  var booleanValueColor = '#000000';
  var nullValueColor = '#000000';

  if (goog.isDef(opt_colors)) {
    propertyNameColor = goog.isDef(opt_colors['propertyName']) ?
        opt_colors['propertyName'] : propertyNameColor;
    stringValueColor = goog.isDef(opt_colors['stringValue']) ?
        opt_colors['stringValue'] : stringValueColor;
    numberValueColor = goog.isDef(opt_colors['numberValue']) ?
        opt_colors['numberValue'] : numberValueColor;
    booleanValueColor = goog.isDef(opt_colors['booleanValue']) ?
        opt_colors['booleanValue'] : booleanValueColor;
    nullValueColor = goog.isDef(opt_colors['nullValue']) ?
        opt_colors['nullValue'] : nullValueColor;
  }

  /**
   * Style information for JSON property names.
   * @type {jsonstyle.JsonStyle}
   * @private
   */
  this.propertyNameStyle_ = new jsonstyle.JsonStyle(
      'goog-jsonprettyprinter-propertyname',
      propertyNameColor);

  /**
   * Style information for JSON property values of type {@code string}.
   * @type {jsonstyle.JsonStyle}
   * @private
   */
  this.stringValueStyle_ = new jsonstyle.JsonStyle(
      'goog-jsonprettyprinter-propertyvalue-string',
      stringValueColor);

  /**
   * Style information for JSON property values of type {@code number}.
   * @type {jsonstyle.JsonStyle}
   * @private
   */
  this.numberValueStyle_ = new jsonstyle.JsonStyle(
      'goog-jsonprettyprinter-propertyvalue-number',
      numberValueColor);

  /**
   * Style information for JSON property values of type {@code boolean}.
   * @type {jsonstyle.JsonStyle}
   * @private
   */
  this.booleanValueStyle_ = new jsonstyle.JsonStyle(
      'goog-jsonprettyprinter-propertyvalue-boolean',
      booleanValueColor);

  /**
   * Style information for JSON property values of type {@code null}.
   * @type {jsonstyle.JsonStyle}
   * @private
   */
  this.nullValueStyle_ = new jsonstyle.JsonStyle(
      'goog-jsonprettyprinter-propertyvalue-null',
      nullValueColor);


  // Install the styles.

  var document = this.dom_.getDocument();

  var element = /** @type {!Element} */ (goog.style.installStyles(
      this.propertyNameStyle_.getCssString(), document));
  this.propertyNameStyle_.setInstalledStyleElement(element);

  element = /** @type {!Element} */ (goog.style.installStyles(
      this.stringValueStyle_.getCssString(), document));
  this.stringValueStyle_.setInstalledStyleElement(element);

  element = /** @type {!Element} */ (goog.style.installStyles(
      this.numberValueStyle_.getCssString(), document));
  this.numberValueStyle_.setInstalledStyleElement(element);

  element = /** @type {!Element} */ (goog.style.installStyles(
      this.booleanValueStyle_.getCssString(), document));
  this.booleanValueStyle_.setInstalledStyleElement(element);

  element = /** @type {!Element} */ (goog.style.installStyles(
      this.nullValueStyle_.getCssString(), document));
  this.nullValueStyle_.setInstalledStyleElement(element);
};

/** @return {string} The color set for JSON property names. */
jsonstyle.JsonStyleManager.prototype.getPropertyNameColor = function() {
  return this.propertyNameStyle_.getColor();
};

/**
 * Set the JSON property name color for the document specified by the
 * {@code goog.dom.DomHelper} used to initialize this {@code JsonStyleManager}.
 * @param {string} color The new color.
 */
jsonstyle.JsonStyleManager.prototype.setPropertyNameColor = function(color) {
  if (color !== this.propertyNameStyle_.getColor()) {
    this.propertyNameStyle_.setColor(color);
    goog.style.setStyles(/** @type {!Element} */
        (this.propertyNameStyle_.getInstalledStyleElement()),
        this.propertyNameStyle_.getCssString());
  }
};

/** @return {string} The color set for JSON string values. */
jsonstyle.JsonStyleManager.prototype.getStringValueColor = function() {
  return this.stringValueStyle_.getColor();
};

/**
 * Set the JSON string value color for the document specified by the
 * {@code goog.dom.DomHelper} used to initialize this {@code JsonStyleManager}.
 * @param {string} color The new color.
 */
jsonstyle.JsonStyleManager.prototype.setStringValueColor = function(color) {
  if (color !== this.stringValueStyle_.getColor()) {
    this.stringValueStyle_.setColor(color);
    goog.style.setStyles(/** @type {!Element} */
        (this.stringValueStyle_.getInstalledStyleElement()),
        this.stringValueStyle_.getCssString());
  }
};

/** @return {string} The color set for JSON number values. */
jsonstyle.JsonStyleManager.prototype.getNumberValueColor = function() {
  return this.numberValueStyle_.getColor();
};

/**
 * Set the JSON number value color for the document specified by the
 * {@code goog.dom.DomHelper} used to initialize this {@code JsonStyleManager}.
 * @param {string} color The new color.
 */
jsonstyle.JsonStyleManager.prototype.setNumberValueColor = function(color) {
  if (color !== this.numberValueStyle_.getColor()) {
    this.numberValueStyle_.setColor(color);
    goog.style.setStyles(/** @type {!Element} */
        (this.numberValueStyle_.getInstalledStyleElement()),
        this.numberValueStyle_.getCssString());
  }
};

/** @return {string} The color set for JSON boolean values. */
jsonstyle.JsonStyleManager.prototype.getBooleanValueColor = function() {
  return this.booleanValueStyle_.getColor();
};

/**
 * Set the JSON boolean value color for the document specified by the
 * {@code goog.dom.DomHelper} used to initialize this {@code JsonStyleManager}.
 * @param {string} color The new color.
 */
jsonstyle.JsonStyleManager.prototype.setBooleanValueColor = function(color) {
  if (color !== this.booleanValueStyle_.getColor()) {
    this.booleanValueStyle_.setColor(color);
    goog.style.setStyles(/** @type {!Element} */
        (this.booleanValueStyle_.getInstalledStyleElement()),
        this.booleanValueStyle_.getCssString());
  }
};

/** @return {string} The color set for JSON null values. */
jsonstyle.JsonStyleManager.prototype.getNullValueColor = function() {
  return this.nullValueStyle_.getColor();
};

/**
 * Set the JSON null value color for the document specified by the
 * {@code goog.dom.DomHelper} used to initialize this {@code JsonStyleManager}.
 * @param {string} color The new color.
 */
jsonstyle.JsonStyleManager.prototype.setNullValueColor = function(color) {
  if (color !== this.nullValueStyle_.getColor()) {
    this.nullValueStyle_.setColor(color);
    goog.style.setStyles(/** @type {!Element} */
        (this.nullValueStyle_.getInstalledStyleElement()),
        this.nullValueStyle_.getCssString());
  }
};
