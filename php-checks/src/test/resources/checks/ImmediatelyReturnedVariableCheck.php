<?php

function f() {
  $result = 1;                 // OK

  $f = function() {
    $result = 1;               // Noncompliant {{Immediately return this expression instead of assigning it to the temporary variable "$result".}}
//  ^^^^^^^
    return $result;
  };

  return $result;
}

function h() {
  $e = new Exception();        // Noncompliant {{Immediately throw this expression instead of assigning it to the temporary variable "$e".}}
  throw $e;
}

function l($p) {

  $a =& $p;                    // Noncompliant {{Immediately return this expression instead of assigning it to the temporary variable "$a".}}
  return $a;
}

function l($p) {
  if (true) {
    $a = $p;                    // Noncompliant {{Immediately return this expression instead of assigning it to the temporary variable "$a".}}
    return $a;
  }
}
function l($p) {
  doSomething();
  if (true) {
    $a = $p;                    // Noncompliant {{Immediately return this expression instead of assigning it to the temporary variable "$a".}}
    return $a;
  }
}

function l($p) {
  $a = 1;
  $a += $p;                    // Noncompliant {{Immediately return this expression instead of assigning it to the temporary variable "$a".}}
  return $a;
}

function l() {
  list($a,, $b) = array(1, 2);  /* Noncompliant */ // $a
//^^^^^^^^^^^^^
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


$result = 1;
return $result;               // OK, global

if (true) {
  $result = 1;
  return $result;               // OK, global
}
