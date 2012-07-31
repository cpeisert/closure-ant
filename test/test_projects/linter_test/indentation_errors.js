goog.provide('lint.errors.MyConstructor');
  goog.require('goog.dom');


  /**
   * Enough lint errors to ruin a suit.
   *
   * @param {string} param1 Description of first parameter.
   * @param {string} param2 Description of second parameter.
   */
lint.errors.MyConstructor = function(param1, param2) {
    var body = goog.dom.getElementsByTagNameAndClass('body')[0];
  var longString = 'The concatenation operator should appear on the ' +
  'previous line.';

  goog.scope(function() { var hidy = 'ho'; });
};
