<?php

class A {

  private $field = 1;

  public static function f() {
    return $this->field;       // NOK
  }

  public function g() {
    return $this->field;       // OK
  }
}

