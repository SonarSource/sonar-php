<?php

  function f() {          // Noncompliant {{This function has 4 returns, which is more than the 3 allowed.}}
//         ^
  if (a) {
    return true;
//  ^^^^^^< {{"return" statement.}}
  } else {
    if (b) {
      return false;
//    ^^^^^^< {{"return" statement.}}
    } else {
      return true;
//    ^^^^^^< {{"return" statement.}}
    }
  }
  return false;
//^^^^^^< {{"return" statement.}}
}

function f() {          // OK
  if (a) {
    return true;
  } else if (b) {
      return false;
  }
  return false;
}

function f() {

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
function f()
{
  if (a) {
    return true;
  } else if (b) {
    return false;
  }
  return false;
}

$function = function() { // Noncompliant {{This function has 4 returns, which is more than the 3 allowed.}}
//          ^^^^^^^^
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
};
