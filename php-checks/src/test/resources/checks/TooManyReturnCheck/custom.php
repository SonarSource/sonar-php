<?php

  function f() {          // Noncompliant {{This function has 4 returns, which is more than the 2 allowed.}}
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

function f() {          // Noncompliant {{This function has 3 returns, which is more than the 2 allowed.}}
  if (a) {
    return true;
  } else if (b) {
      return false;
  }
  return false;
}

function f() {          // Noncompliant {{This function has 3 returns, which is more than the 2 allowed.}}

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

  public function method() { // Noncompliant {{This method has 3 returns, which is more than the 2 allowed.}}
//                ^^^^^^
    if (a) {
      return true;
    } else if (b) {
      return false;
    }
    return false;
  }
}

$a = function () { return 1; }; // OK

if (a) {
  return;
}

// the issue is reported on the function signature and not the function body
function f()  // Noncompliant {{This function has 3 returns, which is more than the 2 allowed.}}
{
  if (a) {
    return true;
  } else if (b) {
    return false;
  }
  return false;
}
