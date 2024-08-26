<?php

function literals() {
  if(true); // Noncompliant {{Replace this expression; used as a condition it will always be constant.}}
  // ^^^^
  if (42); // Noncompliant
  if ("foo"); // Noncompliant
  if ('foo'); // Noncompliant
  $bar = "foo";
  if ("$bar"); // FN, not implemented to avoid FP cases, string interpolation is not interpreted in the tree
  if (null); // Noncompliant
  if (array()); // Noncompliant
  if ([]); // Noncompliant
  if (new stdClass()); // Noncompliant
  if ($foo = 3); // OK, value is assigned
}

function heredoc_strings() {
  // Noncompliant@+1
  if (<<<EOD
  This is a heredoc string.
  EOD
  );
}

function boolean_expressions() {
  if (input() && 42); // Noncompliant
  //             ^^
  $foo = input() && 42; // Noncompliant
  $foo = input() || 42; // Noncompliant
  $foo = input() and 42; // Noncompliant
  $foo = input() or 42; // Noncompliant
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

  42 ?: foo(); // Noncompliant
}

function switch_statement() {
  switch (42) { // Noncompliant
    case 1:
      foo();
      break;
    case 2:
      bar();
      break;
  }
}

function while_statements() {
  while (true) { // OK
    foo();
  }

  do {
    foo();
  } while (true); // OK
}

function variables() {
  $foo = 3 && bar(); // Noncompliant
  $foo = 3 and bar(); // OK, foo = 3 in this case, "and" precedence is lower than "="
  $foo = bar() and 3; // Noncompliant
  $foo = (3 and bar()); // Noncompliant

  $foo = 3 || bar(); // Noncompliant
  $foo = 3 or bar(); // OK, foo = 3 in this case, "or" precedence is lower than "="
  $foo = (3 or bar()); // Noncompliant
  $foo = bar() or 3; // Noncompliant

  $foo = 3 xor bar(); // OK, foo = 3 in this case, "xor" precedence is lower than "="
  $foo = (3 xor bar()); // Noncompliant
  $foo = bar() xor 3; // Noncompliant
}

function anonymous_functions() {
  if (function() { return 42;}); // Noncompliant
}
