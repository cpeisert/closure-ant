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
 * @fileoverview Demo app showing how to use Google Closure Library's unit
 * testing facilities.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */

goog.provide('demo');

goog.require('goog.format.JsonPrettyPrinter');

/**
 * Pretty-print a string of JSON and optionally insert it into an HTML element
 * if specified.
 *
 * @param {*} json The object to pretty print. It could be a JSON object, a
 *     string representing a JSON object, or any other type.
 * @param {!Element=} element Optional HTML element to insert the
 *     pretty-printed JSON.
 * @return {string} The pretty-printed JSON.
 */
demo.prettyPrintJSON = function(json, element) {
  var formatter = new goog.format.JsonPrettyPrinter();
  var formattedJson = formatter.format(json);

  if (goog.isDefAndNotNull(element)) {
    element.innerHTML = formattedJson;
  }
  return formattedJson;
};
