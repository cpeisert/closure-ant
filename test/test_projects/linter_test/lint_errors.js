goog.provide('lint.errors.MyConstructor');
goog.require('goog.dom');

/**
 * enough lint errors to destroy a dryer
 *
 * @param {string} param1 description is not a sentence
 * @param param2 missing type annotation
 */
lint.errors.MyConstructor = function(param1) {
  var body = goog.dom.getElementsByTagNameAndClass("body")[0];
  var longString = 'The concatenation operator should appear on the '
      + 'previous line.';

  var missingSemicolon = "The string to nowhere..."

  var z = body == null ? 'ghost'
      : 'corporeal';
}
