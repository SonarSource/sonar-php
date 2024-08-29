<?php

function literals() {
  if(true); // Noncompliant {{Replace this expression; used as a condition it will always be constant.}}
  // ^^^^
  if (42); // Noncompliant
  if ("foo"); // Noncompliant
  if ('foo'); // Noncompliant
  $bar = "foo";
  if ("$bar"); // FN, not implemented to avoid FP cases, strings are not expanded in the tree
  if (null); // Noncompliant
  if (array()); // Noncompliant
  if ([]); // Noncompliant
  if (new stdClass()); // Noncompliant
  if (__DIR__); // Noncompliant
  if ($foo = 3); // OK, side-effect (value assignment) to the condition
}

function heredoc_strings() {
  // Noncompliant@+1
  if (<<<EOD
  This is a heredoc string.
  EOD
  );

  // Noncompliant@+1
  if (<<<'EOD'
  This is a nowdoc string.
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
  $foo = ~42; // OK
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
}

function conditional_expressions() {
  42 ? foo() : bar(); // Noncompliant
  42 ?: foo(); // Noncompliant
}

function switch_statements() {
  switch (true) { // OK, often used to have more complex conditions in switch statement cases
    case $a < 3:
      foo();
      break;
    case $a > 42:
      bar();
      break;
  }
}

function while_statements() {
  while (true) { // OK, loop should be stopped with break statements
    foo();
  }

  do {
    foo();
  } while (true); // OK
}

function for_statements() {
  for ($i = 1; true; $i++) { // OK, loop should be stopped with break statements, intended by PHP's documentation
    foo();
  }

  for (; ; ) { // OK
    foo();
  }
}

function variables() {
  if ($param); // OK

  $x = 3;
  if ($x); // FN, require reaching definitions analysis
}

function variable_assignments() {
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

function class_migrations() {
  class_alias(NewName::class, OldName::class);
  if (false) { // OK, used to deprecate the old class name
  	class OldName {
  	}
  }
}
