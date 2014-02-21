<?php

$b = false ? (true ? (false ? (true ? 1 : 0) : 0) : 0) : 1;         // NOK

$c = true || false || true || false || false;                       // NOK

$d = true && false && true && false && true && true;                // NOK

function f() {
  if ((true ? 0 : 1) || false || true && false && true || false) {  // NOK
  }
}

for ($i = a ? (b ? (c ? (d ? 1 : 1) : 1) : 1) : 1; i < a; i++) {}   // NOK

$a = $a ? $b : $c                                                   // NOK
.$a ? $b : $c
.$a ? $b : $c
.$a ? $b : $c;

$foo =  [                                                           // OK
  true && true && true && true && true,                             // NOK
  true && true && true && true                                      // OK
];

$e = true | false | true | false;                                   // OK

$a = false ? (true ? (false ? 1 : 0) : 0) : 1;                      // OK


function g() {
  $a = function () {                                                // OK
    $a = true && true;
    $b = true && true;
    $c = true && true;
    $d = true && true;
    $e = true && true;
  };

  return array (true && true && true, true && true && true);        // OK
}


