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
goog.require('ns.soy');
goog.require('soy');


/**
 * Shares a message with the world.
 *
 * @param {string} message The message for the world.
 */
ns.displayMessage = function(message) {
  alert(message);
};

var list = document.querySelectorAll('link');

soy.renderElement(document.querySelector('#mainContent'),
    ns.soy.filterLinkList, {linkList: list, relType: 'stylesheet'});

ns.displayMessage('No dead code elimination here!');
