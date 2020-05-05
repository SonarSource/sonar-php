<?php

class A {

  private $field = 1;

  public static function f() {
    return $this->field;       // Noncompliant {{Remove this use of "$this".}}
//         ^^^^^
  }

  public static function static_anonymous_function() {
    $fn = static function() { return $this->field; }; // Noncompliant
  }

  public static function anonymous_function() {
    $fn = function() { return $this->field; }; // OK
  }

  public function g() {
    return $this->field;       // OK
  }
}

