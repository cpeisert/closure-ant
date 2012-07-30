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

goog.provide('test');

goog.require('mylib');


/**
 * Shares a message with the world.
 *
 * @param {string} message The message for the world.
 */
test.sayHello = function(message) {
  message = mylib.wordwrap(message, 30);
  alert(message);
};

test.sayHello('Hi, guys, this is Eddie your shipboard computer and I ' +
    'just know I\'m gonna get a bundle of kicks out of any program you want ' +
    'to run through me.');
