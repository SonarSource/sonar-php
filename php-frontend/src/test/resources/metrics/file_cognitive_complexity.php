<?php

// file complexity = sum of all the methods

class A {
  function foo() {      // CC: 1
    if ($a) { }                   // +1
  }

  function bar() {      // CC: 3
    function qix() {
      if ($a) { }                 // +2 (incl. +1 for nesting)
    }
    if ($a) { }                   // +1
  }

  function gul() {      // CC: 2
    $func = function($a) {
      if ($a) { }                 // +2 (incl. +1 for nesting)
    };
  }
}

function dom() {        // CC: 1
  if ($a) { }                     // +1
}

$func = function($a) {  // CC: 1
  if ($a) { }                     // +1
};

// rest of the script, with code outside functions
if ($a) {                         // +1
  if ($b) {                       // +2
    $c = $a || $b;                // +1
  }
}
