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

}
