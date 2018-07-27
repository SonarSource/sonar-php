<?php

interface I {
  public function f();                  // Should not visit method delcaration
}

class A {

  public $field;
  public $otherField = $x = 1;

  public function setField($field) {    // OK
    $this->field = $field;
  }

  public function setX() {              // OK
    $field = 1;
    foo($field);
  }

  public function A() {                 // OK (Constructor)
    $field = foo();
    $this->field = $field;
  }

  public static function f1() {
    $field = 1;                         // OK
    $f = function ($field) {};          // OK
  }

  public function f2($param) {          // OK
    $foo = 1;                           // OK
    $field = 1;                         // NOK [[secondary=9]] {{Rename "$field" which has the same name as the field declared at line 9.}}
//  ^^^^^^
  }

  public function f3($field) {          // OK
    if (empty($field)) {
      $field = 1;
    }
    foo($field);
  }

  public function f4() {
    $f1 = function ($field) {           // OK
    };

    $f2 = function () {
      $field = 1;                       // NOK
      $field = 2;                       // OK
    };
  }

  public function f5($param) {
    $field =& $param;                    // NOK
  }

  public function f6() {
    $field = foo();                      // NOK
    callback(function() use ($field)  {  // OK
    });
  }

  public function f7() {
    $f = function () {
      $field = 1;                        // NOK
    };
  }

}

class B {
  public $field;

  public function __construct() {
    $field = foo();                      // OK (Constructor)
    $this->field = $field;
  }

}

$field = 0;                              // OK
$f = function ($field) {};               // OK


$a = new class {

  public $field;

  public function f1($param) {          // OK
    $foo = 1;                           // OK
    $field = 1;                         // NOK [[secondary=-4]] {{Rename "$field" which has the same name as the field declared at line 88.}}
//  ^^^^^^
  }

  public function f3($field) {          // OK
    if (empty($field)) {
      $field = 1;
    }
    foo($field);
  }

  public function f2() {
    $f1 = function ($field) {           // OK
    };

    $f2 = function () {
      $field = 1;                       // NOK
      $field = 2;                       // OK
    };
  }

  public function __construct() {
    $field = foo();                      // OK (Constructor)
    $this->field = $field;
  }

};

$x = new class {                        // OK
  function foo();
};

// nested classes

class A {
  function foo() {
    $x = new class {
      function foo() {}
    };

  }
}

// https://github.com/opencart/opencart/blob/fff9f5522c3b9176d6a23445f9f8f2b25c08712f/upload/system/library/template/twig.php#L42
class Foo {
  public function qix() {
    $bar = new class($options = 0) extends Bar {
      private $options;
    };
  }
}
