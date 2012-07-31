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
goog.provide('lint.errors');
goog.require('goog.dom');
/**
 * enough lint errors to destroy a dryer
 *
 * @param {string} param1 description is not a sentence
 * @param param2 missing type annotation
 */
goog.scope(function() {
  lint.errors.MyConstructor = function(param1) {
    var body = goog.dom.getElementsByTagNameAndClass("body")[0];
    var longString = 'The concatenation operator should appear on the '
        + 'previous line.';

    var missingSemicolon = "The string to nowhere..." // missing semicolon

    var z = body == null ? 'ghost'
        : 'corporeal';
  }
}
);
