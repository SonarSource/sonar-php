<?php

class A {
  function foo($a) { // Noncompliant {{Refactor this function to use "return" consistently.}}
//         ^^^
    if ($a) {
      return false;
//   <^^^^^^^^^^^^^
    }
    return;
// <^^^^^^^
  }
}

function foo($a, $b) {  // Noncompliant {{Refactor this function to use "return" consistently.}}
//       ^^^
    if ($a) {
        return;
//     <^^^^^^^
    } elseif ($b) {
        return;
//     <^^^^^^^
    }
    return true;
// <^^^^^^^^^^^^
}

$func = function($a) {  // Noncompliant {{Refactor this function to use "return" consistently.}}
//      ^^^^^^^^
    if ($a) {
        return;
//     <^^^^^^^
    }
    return 32;
// <^^^^^^^^^^
};

function bar($a) { // Compliant

  $func = function($b) { // Compliant
    return;
  };

  class B {
    function gul($c) { // Compliant
      return;
    }
  }

  function qix($d) { // Compliant
    if ($d) {
      return true;
    }
    return false;
  }

  function lol($e) { // Compliant
    if ($e) {
      return;
    }
    return;
  }

  return false;
}
