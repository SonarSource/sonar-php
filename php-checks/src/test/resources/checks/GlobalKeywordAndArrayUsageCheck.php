<?php

$a;

function f() {
  global $a;           // Noncompliant {{Pass this global variable to the function as a parameter rather than accessing it directly.}}
//^^^^^^^^^^

  $b = $GLOBALS['a'];  // Noncompliant
//     ^^^^^^^^^^^^^

  $GLOBAL = $b;        // OK

  $x = $GLOBALS;       // OK

  return $a + 1;
}
