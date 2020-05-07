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
    $fn->call(new A());
  }

  public static function anonymous_function() {
      $fn = function() { return $this->field; }; // FN - No object gets bound to the closure. To solve this,
       // it has to be verified if this is done anywhere in the subsequent program flow
       // (i.e., call() or Closure::bind() with a valid object is called on it). This is not reliably doable with simple checks.
      $fn();
    }

  public function g() {
    return $this->field;       // OK
  }
}

