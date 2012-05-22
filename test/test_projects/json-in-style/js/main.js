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

goog.provide('test.json.main');

goog.require('goog.dom');
goog.provide('goog.format.JsonPrettyPrinter');

// TODO(cpeisert): create a demo app that lets a user enter JSON in a text box
// and renders the JSON pretty-printed. Also let the user select the
// indentation level.

/**
 * Shares a message with the world.
 *
 * @param {string} json String of JSON text to render.
 * @param {!Element} element The HTML element to insert the rendered JSON.
 * @param {number} indentation Number of spaces to indent each level.
 */
test.main.renderJSON = function(json, element, indentation) {

};