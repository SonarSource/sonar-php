<?php

function f() {           // NOK
  function f() {         // NOK
    // comment
    return 1;
  }
}

$f = function () {       // NOK
  // comment
  return 1;
};

function f() {           // NOK
  // comment
  return 1;
  function f() {         // OK
  }
}

abstract class C {
  public function f() {  // NOK
    // comment
    doSomething();
    return 1;
  }

  public function f() {
    return 1;            // OK
  }

  public function f();   // OK
}

$f = function () {       // OK
  return 1;
};

function f() {           // OK
  return 1;
}
