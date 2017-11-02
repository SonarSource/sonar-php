<?php

function f() {
  return;            // Noncompliant {{Remove the code after this "return".}}
//^^^^^^
  $a;

  if (true) {
    return;          // Noncompliant
    $b;
  } else {
    $c;
  }

  while (true) {
    break;            // Noncompliant {{Remove the code after this "break".}}
    $d;

    continue;         // Noncompliant {{Remove the code after this "continue".}}
//  ^^^^^^^^
    $e;

    if (true) {
      break;
      /** OK and is not affected by comments */
    }

    if (true) {
      continue;  // OK
    }

    if (true)
      break;     // OK

    if (true)
      continue;  // OK
  }

  switch (a) {
  case 1: {
    break;            // Noncompliant
    $e;
  }
  case 2:
    break;       // OK
  case 3:
    break;       // OK
  default:
    $g;
  }

  try {
    $h;
    throw ("MyException");          // Noncompliant
    $i;
  } catch (Exception $e) {
    $j;
    throw ("MyException");
  } finally {
    $k;
  }

  try {
    throw ("MyException");
  } catch (Exception $e) {
    $m;
  }

  if (true)
    return;      // OK
  else
    return;      // OK

  $n; // TODO: NOK - both if branches returns, so this is also unreachable

  return;       // OK

  function f(){
  }

}

function f2() {
  return;;       // OK
}

function f3() {
  return;;            // Noncompliant
  $x;
}

function tagAfterReturn() {
  return; // OK
  ?><?php
}

return;         // Noncompliant

if (true) {}
