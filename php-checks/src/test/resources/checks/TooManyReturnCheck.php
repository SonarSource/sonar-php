<?php

  function f() {          // NOK [[secondary=+3,+6,+8,+11]] {{Reduce the number of returns of this function 4, down to the maximum allowed 2.}}
//^^^^^^^^
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

function f() {          // NOK {{Reduce the number of returns of this function 3, down to the maximum allowed 2.}}
  if (a) {
    return true;
  } else if (b) {
      return false;
  }
  return false;
}

function f() {          // NOK {{Reduce the number of returns of this function 3, down to the maximum allowed 2.}}

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

// the issue is reported on the function signature and not the function body
function f()  // NOK
{
  if (a) {
    return true;
  } else if (b) {
    return false;
  }
  return false;
}
