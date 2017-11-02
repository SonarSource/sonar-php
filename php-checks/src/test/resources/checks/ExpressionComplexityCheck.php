<?php

$b = false ? (true ? (false ? (true ? 1 : 0) : 0) : 0) : 1;         // Noncompliant {{Reduce the number of conditional operators (4) used in the expression (maximum allowed 3).}}
//   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

$c = true || false || true || false || false;                       // Noncompliant [[effortToFix=1]]

$d = true && false && true && false && true && true;                // Noncompliant [[effortToFix=2]]

function f() {
  if ((true ? 0 : 1) || false || true && false && true || false) {  // Noncompliant {{Reduce the number of conditional operators (6) used in the expression (maximum allowed 3).}}
  }
}

for ($i = a ? (b ? (c ? (d ? 1 : 1) : 1) : 1) : 1; i < a; i++) {}   // Noncompliant

$a = $a ? $b : $c                                                   // Noncompliant
.$a ? $b : $c
.$a ? $b : $c
.$a ? $b : $c;

$foo =  [                                                           // OK
  true && true && true && true && true,                             // Noncompliant
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

$a && 
f($d && $e && $f && $g && $h) &&                                    // Noncompliant
$b &&
$c;

myfunction(
    $a                                                              // OK
    .($b ? 'x' : 'y')
    .($c ? 'x' : 'y')
    .($d ? 'x' : 'y')
    .($e ? 'x' : 'y')
);
