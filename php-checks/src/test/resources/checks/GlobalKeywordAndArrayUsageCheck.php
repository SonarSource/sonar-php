<?php

$a;

function f() {
  global $a;           // NOK

  $b = $GLOBALS['a'];  // NOK

  $GLOBAL = $b;        // OK

  return $a + 1;
}
