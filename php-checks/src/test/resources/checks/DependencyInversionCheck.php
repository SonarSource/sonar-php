<?php

class C {

  public function __construct($a, $b) {
    if ($a) {
      if ($b) {
        $object = new \SomeClass();                    // NOK
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
