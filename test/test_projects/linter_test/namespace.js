goog.provide('lint.never.used');

goog.require('superfluous.require');


/**
 * Namespace testing.
 */
lint.namespace.foo = function() {
  var body = goog.dom.getElementsByTagNameAndClass('body')[0];
};


/**
 * Namespace that is not {@code goog.provided}.
 */
not.goog.provided.bar = function() {
  // Do nothing.
};
