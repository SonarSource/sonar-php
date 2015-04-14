<?php

interface I {
  public function f();                  // Should not visit method delcaration
}

class A {

  public $field;

  public function setField($field) {    // OK (Accessor)
    $this->field = $field;
  }

  public function A($field) {           // OK (Constructor)
    $this->$field = $field;
  }

  public static function f1($field) {   // OK
    $field = 1;                         // OK
    $f = function ($field) {};          // OK
  }

  public function f2($param) {          // OK
    $foo = 1;                           // OK
    $field = 1;                         // NOK
  }

  public function f3($field) {}         // NOK

  public function f4() {
    $f1 = function ($field) {           // NOK
    };

    $f2 = function () {
      $field = 1;                       // NOK
      $field = 2;                       // OK
    };
  }

  public function f5($param) {
    $field =& $param;
  }
}

class B {
  public $field;

  public function __construct($field) {  // OK (Constructor)
    $this->field = $field;
  }

}

$field = 0;                              // OK
$f = function ($field) {};               // OK
