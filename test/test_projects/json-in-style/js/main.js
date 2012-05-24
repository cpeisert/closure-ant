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

/*
 * Event handlers for the color popup.
 */

/** @type {goog.ui.Popup} */
jsonstyle.colorPopup = new goog.ui.Popup(
    jsonstyle.colorPopupElement);

/**
 * Make the color-selector popup window visible.
 */
jsonstyle.showPopup = function() {
  /*var popup = jsonstyle.colorPopup;

  var btn = document.getElementById('btn');
  var buttonCorner = toCorner(
      getCheckedValue(document.forms[0].elements['button_corner']));
  var menuCorner = toCorner(
      getCheckedValue(document.forms[0].elements['menu_corner']));

  var t = parseInt(document.getElementById('margin_top').value);
  var r = parseInt(document.getElementById('margin_right').value);
  var b = parseInt(document.getElementById('margin_bottom').value);
  var l = parseInt(document.getElementById('margin_left').value);
  var margin = new goog.math.Box(t, r, b, l);

  popup.setVisible(false);
  popup.setPinnedCorner(menuCorner);
  popup.setMargin(margin);
  popup.setPosition(new goog.positioning.AnchoredViewportPosition(btn,
      buttonCorner));
  popup.setVisible(true);*/
};

/**
 * Initialize the popup used to display the HSV color selector.
 */
jsonstyle.initializeColorPopup = function() {
  jsonstyle.colorPopup.setHideOnEscape(true);
  jsonstyle.colorPopup.setAutoHide(true);

  //goog.events.listen(window, goog.events.EventType.RESIZE, onResize);
};

/**
 * Initialize JSON-In-Style application.
 */
jsonstyle.initializeApp = function() {
  document.getElementById('buttonPropertyName').className = 
      goog.getCssName('.goog-jsonprettyprinter-propertyname');
  document.getElementById('buttonBooleanValue').className =
      goog.getCssName('.goog-jsonprettyprinter-propertyvalue-boolean');
  document.getElementById('buttonNullValue').className =
      goog.getCssName('.goog-jsonprettyprinter-propertyvalue-null');
  document.getElementById('buttonNumberValue').className =
      goog.getCssName('.goog-jsonprettyprinter-propertyvalue-number');
  document.getElementById('buttonStringValue').className =
      goog.getCssName('.goog-jsonprettyprinter-propertyvalue-string');

  jsonstyle.initializeColorPopup();
  goog.events.listen(jsonstyle.inputTextarea, goog.events.EventType.CHANGE,
      jsonstyle.inputTextareaListener);
};

// Application entry point.
jsonstyle.initializeApp();

