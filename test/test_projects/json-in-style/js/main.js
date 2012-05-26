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

goog.provide('jsonstyle');

goog.require('goog.dom');
goog.require('goog.events');
goog.require('goog.events.Event');
goog.require('goog.events.EventType');
goog.require('goog.format.JsonPrettyPrinter');
goog.require('goog.positioning.ClientPosition');
goog.require('goog.positioning.Corner');
goog.require('goog.positioning.AnchoredViewportPosition');
goog.require('goog.ui.Popup');


/** @type {string} */
jsonstyle.colorPopupID = "colorPopup";

/** @type {string} */
jsonstyle.outputElementID = "outputPrettyPrint";

/** @type {string} */
jsonstyle.inputTextareaID = "jsonInputTextarea";

/** @type {!Element} */
jsonstyle.colorPopupElement = /** @type {!Element} */
    (document.getElementById(jsonstyle.colorPopupID));

/** @type {!Element} */
jsonstyle.outputElement = /** @type {!Element} */
    (document.getElementById(jsonstyle.outputElementID));

/** @type {!Element} */
jsonstyle.inputTextarea = /** @type {!Element} */
    (document.getElementById(jsonstyle.inputTextareaID));


/**
 * Pretty-print a string of JSON and insert it into an HTML element.
 *
 * @param {string} json String of JSON to pretty print.
 * @param {!Element} element The HTML element to insert the pretty-printed JSON.
 * @param {number=} indentation Number of spaces to indent each level. Defaults
 *     to 2.
 */
jsonstyle.prettyPrintJSON = function(json, element, indentation) {
  var delimiters = new goog.format.JsonPrettyPrinter.HtmlDelimiters();
  delimiters.indent = indentation || 2;
  var formatter = new goog.format.JsonPrettyPrinter(delimiters);
  element.innerHTML = formatter.format(json);
};

/** @type {goog.ui.Popup} */
jsonstyle.colorPopup = new goog.ui.Popup(jsonstyle.colorPopupElement);


// Event handlers


/**
 * Update the "outputPrettyPrint" &lt;pre&gt; element with pretty-printed JSON
 * based on changes to textarea "jsonInputTextarea".
 *
 * @param {!goog.events.Event} event The change event.
 */
jsonstyle.inputTextareaListener = function(event) {
  try {
    jsonstyle.prettyPrintJSON(jsonstyle.inputTextarea.value,
        jsonstyle.outputElement);
  } catch (error) {
    jsonstyle.outputElement.innerHTML =
        '<span class="error">JSON syntax error.</span>';
  }
};

/**
 * Event handler for window resize.
 *
 * @param {!goog.events.Event} event The resize event.
 */
jsonstyle.onResize = function(event) {
  if (jsonstyle.colorPopup && jsonstyle.colorPopup.isVisible()) {
    jsonstyle.colorPopup.reposition();
  }
}

/**
 * Toggle the visibility of the color-selector popup window (that is, if it is
 * visible then hide it, otherwise make visible). The popup is positioned at
 * the bottom-left corner of the event target.
 *
 * @param {!goog.events.Event} event The click event.
 */
jsonstyle.togglePopup = function(event) {
  var popup = jsonstyle.colorPopup;

  if (popup.isVisible()) {
    popup.setVisible(false);
  } else {
    var targetEl = /** @type {!Element} */ (event.target);

    popup.setVisible(false);
    popup.setPosition(new goog.positioning.AnchoredViewportPosition(
        targetEl, goog.positioning.Corner.BOTTOM_LEFT));
    popup.setVisible(true);
  }
};

/**
 * Initialize the popup used to display the HSV color selector.
 */
jsonstyle.initializeColorPopup = function() {
  jsonstyle.colorPopup.setAutoHide(false);
  jsonstyle.colorPopup.setHideOnEscape(true);
  jsonstyle.colorPopup.setPinnedCorner(goog.positioning.Corner.TOP_LEFT);
  jsonstyle.colorPopup.setMargin(8, 0, 0, 5);
};

/**
 * Initialize JSON-In-Style application.
 */
jsonstyle.initializeApp = function() {
  jsonstyle.initializeColorPopup();

  var buttonPropertyName = /** @type {!Element} */
      document.getElementById('buttonPropertyName');
  var buttonBooleanValue = /** @type {!Element} */
      document.getElementById('buttonBooleanValue');
  var buttonNullValue = /** @type {!Element} */
      document.getElementById('buttonNullValue');
  var buttonNumberValue = /** @type {!Element} */
      document.getElementById('buttonNumberValue');
  var buttonStringValue = /** @type {!Element} */
      document.getElementById('buttonStringValue');

  buttonPropertyName.className =
      goog.getCssName('goog-jsonprettyprinter-propertyname');
  buttonBooleanValue.className =
      goog.getCssName('goog-jsonprettyprinter-propertyvalue-boolean');
  buttonNullValue.className =
      goog.getCssName('goog-jsonprettyprinter-propertyvalue-null');
  buttonNumberValue.className =
      goog.getCssName('goog-jsonprettyprinter-propertyvalue-number');
  buttonStringValue.className =
      goog.getCssName('goog-jsonprettyprinter-propertyvalue-string');

  goog.events.listen(buttonPropertyName, goog.events.EventType.CLICK,
      jsonstyle.togglePopup);
  goog.events.listen(buttonBooleanValue, goog.events.EventType.CLICK,
      jsonstyle.togglePopup);
  goog.events.listen(buttonNullValue, goog.events.EventType.CLICK,
      jsonstyle.togglePopup);
  goog.events.listen(buttonNumberValue, goog.events.EventType.CLICK,
      jsonstyle.togglePopup);
  goog.events.listen(buttonStringValue, goog.events.EventType.CLICK,
      jsonstyle.togglePopup);

  goog.events.listen(jsonstyle.inputTextarea, goog.events.EventType.CHANGE,
      jsonstyle.inputTextareaListener);
  goog.events.listen(window, goog.events.EventType.RESIZE, jsonstyle.onResize);
};

// Application entry point.
jsonstyle.initializeApp();
