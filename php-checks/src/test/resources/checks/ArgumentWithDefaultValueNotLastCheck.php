<?php

function f($p1, $p2 = 2, $p3) {           // Noncompliant {{Move arguments "$p2" after arguments without default value}}
//        ^^^^^^^^^^^^^^^^^^^
  return;
}

function f($p1 = 1, $p2, $p3 = 3, $p4) {   // Noncompliant {{Move arguments "$p1", "$p3" after arguments without default value}}
  return;
}

function g($p1, $p2 = 2) {               // OK
  return;
}

function foo($p = 42, ... $rest) {  // OK, parameter with variable number of arguments is always last
}
