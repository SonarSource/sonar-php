<?php

  class KO { // Noncompliant {{The Cyclomatic Complexity of this class "KO" is 6 which is greater than 5 authorized, split this class.}} [[effortToFix=1]]
//^^^^^
  function f() // +1
  {
    switch (foo)
    {
      case 1: // +1
      case 2: // +1
      default:
      ;
    }

    if (true) { // +1
      return 1 && 2; // +1
    }
    return 1;
  }

  function f() // +1
  {
  }
}

class KO2 { // Noncompliant [[effortToFix=2]]
  function f() {  // +1
    return true && true && true && true && true && true && true; // +6
  }
}

class OK {

  public function f() // +1
  {
    switch (foo)
    {
      case 1: // +1
      case 2: // +1
      default:
      ;
    }
  }

  public function ok() {
  }
}

class OK {
}

$x = new class {        // Noncompliant {{The Cyclomatic Complexity of this anonymous class is 6 which is greater than 5 authorized, split this class.}}
//       ^^^^^
  function f1() {}
//^^^^^^^^< {{+1}}
  function f2() {}
//^^^^^^^^< {{+1}}
  function f3() {}
//^^^^^^^^< {{+1}}
  function f4() {}
//^^^^^^^^< {{+1}}
  function f5() {}
//^^^^^^^^< {{+1}}
  function f6() {}
//^^^^^^^^< {{+1}}
};
