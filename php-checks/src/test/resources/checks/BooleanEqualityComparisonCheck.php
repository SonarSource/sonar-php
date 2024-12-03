<?php
!true;            // Noncompliant {{Remove the literal "true" boolean value.}}
!false;           // Noncompliant {{Remove the literal "false" boolean value.}}
a == false;       // Noncompliant
//   ^^^^^
a == true;        // Noncompliant
a != false;       // Noncompliant
a != true;        // Noncompliant
false == a;       // Noncompliant
true == a;        // Noncompliant
false != a;       // Noncompliant
true != a;        // Noncompliant
false && foo();   // Noncompliant
foo() || true;    // Noncompliant
a == true == b;   // Noncompliant

a || true         // Noncompliant
  || b
  || true;        // Noncompliant
(a || true) ? b : c; // Noncompliant
true xor a;       // Noncompliant
$a xor false;     // Noncompliant

a === false;      // OK - exception
a === true;       // OK - exception
a !== false;      // OK - exception
a !== true;       // OK - exception
a == foo(true);   // OK
true < 0;         // OK
~true;            // OK
++ true;          // OK
!foo;             // OK
foo() && bar();   // OK
a ? true : b;     // OK
a ? b : false;    // OK
a ? : true;       // OK
