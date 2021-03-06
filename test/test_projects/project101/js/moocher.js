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

// This file is a "moocher" because it does not goog.provide any namespaces.

goog.require('goog.date.Date');
goog.require('goog.dom');


/**
 * Shares a message with the world.
 *
 * @param {string} message The message for the world.
 */
var moocherHelloWorld = function(message) {
  var weekNumber = new goog.date.Date().getWeekNumber();
  var p = goog.dom.createDom('p', undefined, 'week ' + weekNumber + ': ' +
      message);
  goog.dom.appendChild(goog.dom.getElement('div_main'), p);
};

moocherHelloWorld('It is satisfying to mooch off of goog.date.Date and ' +
    'goog.dom.');
