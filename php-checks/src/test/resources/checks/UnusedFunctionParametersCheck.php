<?php

function f($p1, $p2, $p3) {              // NOK  -  $p1
    $p2 = 1;
    call($p3);
}

$a = function($p1, $p2) { return $p1; }; // NOK  -  $p2


function f($p1, $p2) {                   // NOK  -  $p1
  function nestedF($p1, $p2) {           // NOK  -  $p2
    $p1 = 1;
  }
  return $p2;
}

abstract class C {

  public function f1($p1, $p2, $p3) {    // NOK  -  $p2, $p3
      return $p1;
  }

  public function f1($p1, $p2, $p3, $var) {    // OK
      // FIXME: tests "SEMI_COMPLEX_RECOVERY_EXPRESSION"
      doSomething("${var}");
      return "$p1 ${p2} {$p3}";
  }

  /*
   * @inheritdoc
   */
  public function f2($p1) {              // OK
    return 1;
  }

  public function f3($p1);               // OK
}

function f($p1, $p2) {                   // OK
  $p1 = $p2;
}

class D extends A {
  public function f1($p1) {} // OK
  private function f2($p1) {} // NOK
}

class E implements B {
  public function f1($p1) {} // OK
}
