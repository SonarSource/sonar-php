<?php

class A {

  private function __construct() {  // OK - private constructor
  }

  private function f() {            // NOK {{Remove this unused private "f" method.}}
    $foo = clone $this;

    $code = '_i';
    echo "${foo->j()}";
    return $foo->g(1)
               ->h();
  }

  private function g($p1) {         // OK
  }

  private function h() {            // OK
  }

  private function _i() {           // OK - used in a simple string literal
  }

  private function j() {            // OK - used as encapsulated variable in string
  }

  public function k() {             // OK - public
  }

  public function __i() {           // OK - magic method
  }
}

class B {

  private $field;

  private function B() {              // OK - private constructor
  }


}
