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

goog.provide('ns');

goog.require('goog.dom');
goog.require('goog.events');
goog.require('ns.soy');
goog.require('soy');

// goog.require('goog.soy');


/**
 * Print strings separated by commas or whitespace as an unordered list.
 *
 * @param {string} items The strings separated by commas or whitespace.
 */
ns.printStringAsUnorderedList = function(items) {
  var itemArray = items.split(/\s+|,/);
  soy.renderElement(document.querySelector('#listOutput'),
      ns.soy.printListAsUL, {'list': itemArray});

  /*goog.soy.renderElement(document.querySelector('#listOutput'),
  ns.soy.printListAsUL, {'list': itemArray});*/
};


/**
 * Handler for itemsInput change event.
 */
ns.onInputChange = function() {
  var itemsInputEl = document.querySelector('#itemsInput');
  ns.printStringAsUnorderedList(itemsInputEl.value);
};


/**
 * Entry point for app.
 */
ns.main = function() {
  var itemsInputEl = document.querySelector('#itemsInput');
  goog.events.listen(
      itemsInputEl,
      goog.events.EventType.CHANGE,
      ns.onInputChange);
};

ns.main();
