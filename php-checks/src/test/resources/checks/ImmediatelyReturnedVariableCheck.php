<?php

function f() {
  $result = 1;                 // OK

  $f = function() {
    $result = 1;               // NOK
    return $result;
  };

  return $result;
}

function h() {
  $e = new Exception();        // NOK
  throw $e;
}

function l($p) {

  $a =& $p;                    // NOK
  return $a;
}

function l() {
  list($a, $b) = array(1, 2);  // NOK $a
  return $a;
}

function l() {
  list($a->b) = array(1, 2);   // OK
  return $a;
}

function l() {
  list(list($a)) = array(1, 2);  // OK - not covered
  return $a;
}

function f() {
  $result = 1;                 // OK
  return 1;
}


function g() {
  $result = 1;                 // OK
  doSomething($result);

  return $result;
}

function h() {
  $e = new Exception();        // OK

  if (1) {
    throw $e;
  }
}

function l() {
  $a = 1 ;                     // OK
  return;
}

function l() {
  $a['a'] = 1 ;                // OK
  return $a;
}

function l() {
  $a['a'] = 1 ;                // OK - not covered
  return $a['a'];
}

function l() {
  if (1)
    $result = 1;               // OK

  return $result;
}


function l() {
  $result->a = 1 ;             // OK
  return $result;
}

function l() {
  ++$result = 1 ;              // OK - not covered
  return $result;
}

$result = 1;
