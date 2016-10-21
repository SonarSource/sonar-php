<?php

/**
 * GLOBAL SCOPE $a, $b, f, A, foo, Foo
 */
$a = 1;

if (true) {
  $b = 1;
}


/**
 * FUNCTION SCOPE $p1, $p2, $b, $externalVariable, $c, $d
 */
function f($p1, &$p2) {
  global $b, $externalVariable, $$varVar;    // only variable identifiers are supported in global statement
  $c = 1;
  $

  /**
   * FUNCTION EXPRESSION SCOPE $c, $b, $e
   */
  $d = function () use ($c, &$b) {
    $e = 1;
  };
}

/**
 * CLASS SCOPE $field1, method
 */

class A {

  public $field1;

  public function method() {
    $g = 1;
  }

}

/**
 * ANONYMOUS CLASS SCOPE $field1, method
 */

$a = new class {

  public $field1;

  public function method() {
    $g = 1;
  }

};
