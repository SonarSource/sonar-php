<?php

function f() {
  return;            // NOK {{Remove the code after this "return".}}
//^^^^^^
  $a;

  if (true) {
    return;          // NOK
    $b;
  } else {
    $c;
  }

  while (true) {
    break;            // NOK {{Remove the code after this "break".}}
    $d;

    continue;         // NOK {{Remove the code after this "continue".}}
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
    break;            // NOK
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
    throw ("MyException");          // NOK
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
  return;;            // NOK
  $x;
}

return;         // NOK

if (true) {}
