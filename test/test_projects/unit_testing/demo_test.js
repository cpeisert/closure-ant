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
 * @fileoverview Unit tests for JSON pretty print demo.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */

goog.require('demo');
goog.require('goog.testing.jsunit');

// Global function names beginning with "test" are registered as tests by the
// framework. These functions should not declare any arguments.
var testJsonNull = function() {
  assertEquals('', demo.prettyPrintJSON(null));
};

var testJsonObject = function() {
  assertEquals('{\n  "key": "value"\n}',
      demo.prettyPrintJSON('{"key": "value"}'));
};

var testJsonArray = function() {
  assertEquals('[\n  null,\n  false,\n  35\n]',
      demo.prettyPrintJSON('[null, false, 35]'));
};