<?php

  function f() {           // Noncompliant {{This function "f" has 6 lines, which is greater than the 3 lines authorized. Split it into smaller functions.}}
//^^^^^^^^^^^^
  function f() {         // Noncompliant
    echo 'hello';
    // comment
    return 1;
  }
}

$f = function () {       // Noncompliant {{This function expression has 4 lines, which is greater than the 3 lines authorized. Split it into smaller functions.}}
//   ^^^^^^^^^^^
  echo 'hello';
  // comment
  return 1;
};

function f() {           // Noncompliant
  // comment
  return 1;
  function f() {         // OK
  }
}

abstract class C {
  public function f() {  // Noncompliant {{This function "f" has 4 lines, which is greater than the 3 lines authorized. Split it into smaller functions.}}
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
