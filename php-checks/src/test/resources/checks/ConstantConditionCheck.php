<?php

function literals() {
  if(true); // Noncompliant {{Replace this expression; used as a condition it will always be constant.}}
  // ^^^^
  if (42); // Noncompliant
  if ("foo"); // Noncompliant
  if (null); // Noncompliant
  if (array()); // Noncompliant
  if (new stdClass()); // Noncompliant
}

function boolean_expressions() {
  if (input() || 42); // Noncompliant
  //             ^^
  $foo = input() || 42; // Noncompliant
  $foo = input() && 42; // Noncompliant
  $foo = input() or 42; // Noncompliant
  $foo = input() and 42; // Noncompliant
  $foo = input() xor 42; // Noncompliant
  $foo = !42; // Noncompliant
}

function alternative_if_statements() {
  if (input()) {
    foo();
  } else if (42) { // Noncompliant
    bar();
  }

  if (input()) {
    foo();
  } elseif (42) { // Noncompliant
    bar();
  }

  42 ? foo() : bar(); // Noncompliant
}
