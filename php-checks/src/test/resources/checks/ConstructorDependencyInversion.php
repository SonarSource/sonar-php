<?php

class C {

  public function __construct($a, $b) {
    if ($a) {
      if ($b) {
        $object = new \SomeClass();                    // Noncompliant {{Remove this creation of object in constructor. Use dependency injection instead.}}
        $object = new SomeClass();                     // Noncompliant
//                ^^^
        $object = new Package\SomeOtherClass();        // Noncompliant
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
        $object = new SomeClass();                     // Noncompliant
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
    $x = new class {                // Noncompliant
      function foo() {
        new Foo();                  // OK
      }

      function __construct() {
        new Foo();                  // Noncompliant
      }

      function bar() {
        new Foo();                  // OK
      }
    };
  }

  function foo() {
    $x = new class {                // OK
      function __construct() {
        new Foo();                  // Noncompliant
      }

      function bar() {
        new Foo();                  // OK
      }
    };
  }
}
