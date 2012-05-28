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
goog.require('goog.dom.DomHelper');
goog.require('goog.events');
goog.require('goog.events.Event');
goog.require('goog.events.EventType');
goog.require('goog.format.JsonPrettyPrinter');
goog.require('goog.positioning.ClientPosition');
goog.require('goog.positioning.Corner');
goog.require('goog.positioning.AnchoredViewportPosition');
goog.require('goog.style');
goog.require('goog.ui.Component');
goog.require('goog.ui.HsvPalette');
goog.require('goog.ui.Popup');

goog.require('jsonstyle.JsonStyleManager');


/** @type {goog.ui.HsvPalette} */
jsonstyle.colorPalette;

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

/** @type {jsonstyle.JsonStyleManager} */
jsonstyle.jsonStyleManager;


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
  delimiters.indent = goog.isDef(indentation) ? indentation : 2;
  var formatter = new goog.format.JsonPrettyPrinter(delimiters);
  element.innerHTML = formatter.format(json);
};

/** @type {goog.ui.Popup} */
jsonstyle.colorPopup = new goog.ui.Popup(jsonstyle.colorPopupElement);


// Event handlers


/**
 * Update the {@code outputPrettyPrint} &lt;pre&gt; element with pretty-printed
 * JSON based on changes to textarea "jsonInputTextarea". On error, update
 * {@code outputPrettyPrint} with the message "JSON syntax error."
 *
 * @param {!goog.events.Event} event The change event.
 */
jsonstyle.onTextareaChange = function(event) {
  try {
    jsonstyle.prettyPrintJSON(jsonstyle.inputTextarea.value,
        jsonstyle.outputElement);
  } catch (error) {
    jsonstyle.outputElement.innerHTML =
        '<span class="' + goog.getCssName('error')
        + '">JSON syntax error.</span>';
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
};

/**
 * Update the appropriate JSON style color based on which button is clicked and
 * the selected color from the color palette.
 *
 * @param {!goog.events.Event} event The click event.
 */
jsonstyle.onColorPaletteAction = function(event) {
  if (goog.isDef(jsonstyle.colorPalette.buttonClickedId)) {
    var color = jsonstyle.colorPalette.getColor();

    switch (jsonstyle.colorPalette.buttonClickedId) {
      case "buttonPropertyName":
        jsonstyle.jsonStyleManager.setPropertyNameColor(color);
        break;
      case "buttonBooleanValue":
        jsonstyle.jsonStyleManager.setBooleanValueColor(color);
        break;
      case "buttonNumberValue":
        jsonstyle.jsonStyleManager.setNumberValueColor(color);
        break;
      case "buttonNullValue":
        jsonstyle.jsonStyleManager.setNullValueColor(color);
        break;
      case "buttonStringValue":
        jsonstyle.jsonStyleManager.setStringValueColor(color);
        break;
      default: throw Error("unrecognized button ID: "
          + jsonstyle.colorPalette.buttonClickedId);
    }
  }
};

/**
 * Toggle the visibility of the color-selector popup window (that is, if it is
 * visible then hide it, otherwise make visible). The popup is positioned at
 * the bottom-left corner of the event target.
 *
 * @param {!goog.events.Event} event The click event.
 */
jsonstyle.togglePopup = function(event) {
  var popup = jsonstyle.colorPopup;
  var buttonEl = /** @type {!Element} */ (event.target);
  if (!goog.string.startsWith(buttonEl.getAttribute("id"), 'button')) {
    buttonEl = /** @type {!Element} */ (event.target.parentNode);
  }

  if (popup.isVisible()) {
    popup.setVisible(false);
    jsonstyle.colorPalette.buttonClickedId = undefined;
  } else {
    popup.setVisible(false);
    popup.setPosition(new goog.positioning.AnchoredViewportPosition(
        buttonEl, goog.positioning.Corner.BOTTOM_LEFT));
    popup.setVisible(true);

    var buttonId = buttonEl.getAttribute("id");
    var color = jsonstyle.getButtonColorByButtonID(buttonId);

    jsonstyle.colorPalette.buttonClickedId = buttonId;
    jsonstyle.colorPalette.setColor(color);
  }
};

/**
 * Get the button font color for the button with the specified ID.
 * @param buttonId The button ID.
 * @return {string} The button's font color.
 */
jsonstyle.getButtonColorByButtonID = function(buttonId) {
  switch (buttonId) {
    case "buttonPropertyName":
      return jsonstyle.jsonStyleManager.getPropertyNameColor();
      break;
    case "buttonBooleanValue":
      return jsonstyle.jsonStyleManager.getBooleanValueColor();
      break;
    case "buttonNumberValue":
      return jsonstyle.jsonStyleManager.getNumberValueColor();
      break;
    case "buttonNullValue":
      return jsonstyle.jsonStyleManager.getNullValueColor();
      break;
    case "buttonStringValue":
      return jsonstyle.jsonStyleManager.getStringValueColor();
      break;
    default: throw Error("unrecognized button ID: " + buttonId);
  }
};


// Initialize the JSON in Style application.


/**
 * Initialize the popup used to display the HSV color selector.
 */
jsonstyle.initializeColorPopup = function() {
  var popup = jsonstyle.colorPopup;
  
  popup.setAutoHide(false);
  popup.setHideOnEscape(true);
  popup.setPinnedCorner(goog.positioning.Corner.TOP_LEFT);
  popup.setMargin(8, 0, 0, 5);

  var domHelper = goog.dom.getDomHelper(jsonstyle.colorPopupElement);
  jsonstyle.colorPalette = new goog.ui.HsvPalette(domHelper, 'blue',
      goog.getCssName('goog-hsv-palette-sm'));
  jsonstyle.colorPalette.render(jsonstyle.colorPopupElement);
  /** @type {string|undefined} */
  jsonstyle.colorPalette.buttonClickedId = undefined;
  goog.events.listen(jsonstyle.colorPalette, goog.ui.Component.EventType.ACTION,
      jsonstyle.onColorPaletteAction);
};

/**
 * Initialize application.
 */
jsonstyle.initializeApp = function() {
  var domHelper = goog.dom.getDomHelper(jsonstyle.outputElement);
  jsonstyle.jsonStyleManager = new jsonstyle.JsonStyleManager(domHelper, {
    'propertyName': 'blue',
    'stringValue': 'darkgreen',
    'numberValue': 'darkorange',
    'booleanValue': 'darkviolet',
    'nullValue': 'red'});

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
      jsonstyle.onTextareaChange);
  goog.events.listen(window, goog.events.EventType.RESIZE, jsonstyle.onResize);
};

// Application entry point.
window.onload = function() {
  jsonstyle.initializeApp();
};
