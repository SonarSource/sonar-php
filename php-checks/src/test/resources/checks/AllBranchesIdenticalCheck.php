<?php

function ternary() {
  echo($a ? foo() : bar());
  echo($a ? foo() : foo()); // Noncompliant
//     ^^^^

  $condition ? ($nestedCondition ? foo1() : foo2()) : ($nestedCondition ? foo1() : foo2()); // Noncompliant
  $condition ? ($nestedCondition ? foo1() : foo1()) : foo2(); // Noncompliant

  false
     ? false       // Noncompliant
        ? foo($b)
        : foo($b)
     : foo(5);
}

function if_statements() {
  if ($a) {
    foo();
  }

  if ($a) {
    foo();
  } else {
    bar();
  }

  if ($a) { // Noncompliant
//^^
    foo();
  } else {
    foo();
  }

  if ($a) {
    foo();
  } else if ($b) {
    foo();
  }

  if ($a) { // Noncompliant
    foo();
  } else if ($b) {
    foo();
  } else {
    foo();
  }

  if ($a) {
    foo();
  } elseif ($b) {
    bar();
  } else {
    foo();
  }

  if ($a) { // Noncompliant
    foo();
  } elseif ($b) {
    foo();
  } else {
    foo();
  }
  
  // SONARPHP-782

  if(a == 1) { // OK, raised by S1871
    doSomething();
  } else if (a == 2) {
    doSomething();
  } else if (a == 3) {
    doSomething();
  }

}

function switch_statements() {

  switch($a) {
  }

  switch($a) {
    case 1:
      foo();
      break;
  }

  switch($a) { // Noncompliant
    default:
      foo();
  }

  switch($a) { // Noncompliant
//^^^^^^
    case 1:
      foo();
      break;
    default:
      foo();
      break;
  }

  switch($a) {
    case 1:
      foo();
      break;
    default:
      bar();
      break;
  }

  switch($a) { // Noncompliant
    case 1:
      foo();
      break;
    case 2:
      foo();
      break;
    default:
      foo();
  }

  switch($a) {
    case 1:
      foo();
    default:
      foo();
  }
  // SONARPHP-782

  switch($a) { // OK, raised by S1871
    case 1:
      doTheThing();
      break;
    case 2:
      doTheThing();
      break;
    case 3:
      doTheThing();
      break;  
  }

  switch($a) { // OK, raised by S1871
    case 1:
      doTheThing();
    case 2:
      doTheThing();
    case 3:
      doTheThing();
  }

}
