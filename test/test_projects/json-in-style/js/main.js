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

goog.provide('test.jsonstyle');

goog.require('goog.dom');
goog.require('goog.events');
goog.provide('goog.format.JsonPrettyPrinter');


/** @type {string} */
test.jsonstyle.outputElementID = "outputPrettyPrint";

/** @type {string} */
test.jsonstyle.inputTextareaID = "jsonInputTextarea";

/** @type {!Element} */
test.jsonstyle.outputElement = document.getElementById(
    test.jsonstyle.outputElementID);

/** @type {!Element} */
test.jsonstyle.inputTextarea = document.getElementById(
    test.jsonstyle.inputTextareaID);


/**
 * Pretty-print a string of JSON and insert it into an HTML element.
 *
 * @param {string} json String of JSON to pretty print.
 * @param {!Element} element The HTML element to insert the pretty-printed JSON.
 * @param {number=} indentation Number of spaces to indent each level. Defaults
 *     to 2.
 */
test.jsonstyle.prettyPrintJSON = function(json, element, indentation) {
  var delimiters = new goog.format.JsonPrettyPrinter.HtmlDelimiters();
  delimiters.indent = indentation || 2;
  var formatter = new goog.format.JsonPrettyPrinter(delimiters);
  element.innerHTML = formatter.format(json);
};

/**
 * Update the "outputPrettyPrint" &lt;pre&gt; element with pretty-printed JSON
 * based on changes to textarea "jsonInputTextarea".
 *
 * @param {!Event} event The change event.
 */
test.jsonstyle.jsonInputTextareaListener = function(event) {
  try {
    var prettyJSON = test.jsonstyle.prettyPrintJSON(
        text.jsonstyle.inputTextarea.value, test.jsonstyle.outputElement);
    test.jsonstyle.outputElement.innerHTML = prettyJSON;
  } catch (error) {
    test.jsonstyle.outputElement.innerHTML =
        '<span class="error">JSON syntax error.</span>';
  }
}

goog.events.listen(test.jsonstyle.outputElement, goog.events.EventType.CHANGE,
    test.jsonstyle.jsonInputTextareaListener(event));

