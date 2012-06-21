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

goog.provide('mylib');

/**
 * Wrap text at {@code width} characters without splitting words.
 *
 * @param {string} text The text to wrap.
 * @param {number} width The maximum number of characters per line.
 * @param {string} lineSeparator The text to use at the end of each line. For
 *     example, '\n' or '&lt;br&gt;'.
 * @return {String}
 */
mylib.wordwrap = function (text, width, lineSeparator) {
  if (text == null) {
    throw Error("text is null");
  }

  lineSeparator = lineSeparator || '\n';
  width = width || 75;

  var regex = '.{1,' + width + '}(\\s|$)|\\S+?(\\s|$)';

  return text.match(RegExp(regex, 'g')).join(lineSeparator);
};
