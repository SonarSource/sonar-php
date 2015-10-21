<?php

/**
 * GLOBAL SCOPE $a, $b, f, A, foo, Foo
 */
$a = 1;

if (true) {
  $b = 1;
}


/**
 * FUNCTION SCOPE $p1, $p2, $c, $d, $e
 */
function f($p1, &$p2) {
  $c = 1;

  $d = function () {
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
 * SAME NAME: for different kind of symbols in same scope
 */

function foo() {}

class Foo {}
