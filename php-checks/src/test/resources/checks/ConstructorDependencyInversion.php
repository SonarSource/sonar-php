<?php

class C {

  public function __construct($a, $b) {
    if ($a) {
      if ($b) {
        $object = new \SomeClass();                    // NOK {{Remove this creation of object in constructor. Use dependency injection instead.}}
        $object = new SomeClass();                     // NOK
//                ^^^
        $object = new Package\SomeOtherClass();        // NOK
      } else {
        throw new InvalidArgumentException();
      }
    }
  }

  public function f() {
    $object = new SomeClass();                          // OK
  }
}

class D {

  public function D($a, $b) {
        $object = new SomeClass();                     // NOK
  }

  public function f() {
    $object = new SomeClass();                          // OK
  }
}

class ClassWithoutConstructor {

  public function f() {
    $object = new SomeClass();                          // OK
  }
}

$object = new SomeClass();                             // OK

class A {
  function __construct() {
    $x = new class {                // NOK
      function foo() {
        new Foo();                  // OK
      }

      function __construct() {
        new Foo();                  // NOK
      }

      function bar() {
        new Foo();                  // OK
      }
    };
  }

  function foo() {
    $x = new class {                // OK
      function __construct() {
        new Foo();                  // NOK
      }

      function bar() {
        new Foo();                  // OK
      }
    };
  }
}
