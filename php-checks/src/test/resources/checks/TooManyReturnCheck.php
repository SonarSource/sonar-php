<?php

function f() {          // NOK
  if (a) {
    return true;
  } else {
    if (b) {
      return false;
    } else {
      return true;
    }
  }
  return false;
}

function f() {          // OK
  if (a) {
    return true;
  } else if (b) {
      return false;
  }
  return false;
}

function f() {          // OK

  function nestedF() {
    return true;        // Should not count return of nested function in enclosing function return counter
  }

  if (a) {
    return true;
  } else if (b) {
    return false;
  }
  return false;
}

class C {

  public function f() {
      return false;     // OK
  }
}

$a = function () { return 1; }; // OK

if (a) {
  return;
}
