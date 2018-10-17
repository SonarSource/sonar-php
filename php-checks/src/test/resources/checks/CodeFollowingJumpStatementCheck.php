<?php

function f() {
  echo "hello";
  return;
//^^^^^^^>
  $a;            // Noncompliant {{Remove this unreachable code.}}
//^^^

  if (true) {
    return;
    $b;         // Noncompliant
  } else {
    $c;
  }

  while (true) {
    break;
    $d;            // Noncompliant

    continue;
    $e;            // Noncompliant

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
    $e;  // Noncompliant
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
    $i;          // Noncompliant
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
    return;
//  ^^^^^^^>
  else
    return;
//  ^^^^^^^>

  $n; // Noncompliant
//^^^

  return;

  function f(){ // Noncompliant
  }

}

function f2() {
  return;;       // OK
}

function f3() {
  return;;
  $x;            // Noncompliant
}

function tagAfterReturn() {
  return; // OK
  ?><?php
}

function breakAfterJump() {
  switch($a) {
    case 0:
      return "x";
      break;
    case 1:
      return "y";
      break;
      foo(); // Noncompliant
    default:
      return "z";
      break;
  }
}

function labeledStatement() {
  if ($a == 5) {
    goto error;
  }
  doSomething();
  return;
  error:
    print("don't use 5");
    somethingElse();
}

return;

if (true) {}         // Noncompliant

function foreachLoop(){
    foreach ($loaders as $loader) {
        return foo($loader);
    }

    return false;
}

function classIsNotDead() {
  return new A();

  class A {}
}

function functionIsDead() {
  return 42;

  function foo() {}// Noncompliant
}

function invalidJump() {
  continue;
}

$functionExpression = function() {
  switch ($x) {
     case 1:
       return 1;
     case 2:
       return 2;
     default:
       return 0;
  }

  return 42; // Noncompliant
};

class A {
  function methodWithoutBody();
  function method() {
    return 42;
    echo 42; // Noncompliant
  }
}

function return_in_try() {
  try {
    foo();
    return;
  } catch (Exp $e) {
    dosmth();
  }
}
