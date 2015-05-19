<?php

function f() {
  return;
  $a;            // NOK

  if (true) {
    return;
    $b;          // NOK
  } else {
    $c;
  }

  while (true) {
    break;
    $d;          // NOK

    continue;
    $e;          // NOK

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
    break;
    $e;          // NOK
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
    throw ("MyException");
    $i;          // NOK
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

  return;

  function f(){  // OK
  }

}

function f2() {
  return;;       // OK
}

function f3() {
  return;;
  $x;            // NOK
}
