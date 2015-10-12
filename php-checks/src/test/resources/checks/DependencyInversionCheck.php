<?php

class C {

  public function __construct($a, $b) {
    if ($a) {
      if ($b) {
        $object = new \SomeClass();                    // NOK {{Remove this creation of object in constructor. Use dependency injection instead.}}
        $object = new SomeClass();                     // NOK
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

class C {

  public function C($a, $b) {
        $object = new SomeClass();                     // NOK
  }

  public function f() {
    $object = new SomeClass();                          // OK
  }
}
