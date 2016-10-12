<?php

  function f() {           // NOK {{This function "f" has 7 lines, which is greater than the 3 lines authorized. Split it into smaller functions.}}
//^^^^^^^^^^^^
  function f() {         // NOK
    // comment
    return 1;
  }
}

$f = function () {       // NOK {{This function expression has 5 lines, which is greater than the 3 lines authorized. Split it into smaller functions.}}
//   ^^^^^^^^^^^
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
  public function f() {  // NOK {{This function "f" has 6 lines, which is greater than the 3 lines authorized. Split it into smaller functions.}}
//       ^^^^^^^^^^^^
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
