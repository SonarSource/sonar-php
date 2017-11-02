<?php

$myObject->define();     // OK
$myObject->define("Foo", true); // OK
call("CONST");           // OK

const Foo = false;       // Noncompliant {{Rename this constant "Foo" to match the regular expression ^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$.}}
//    ^^^
const FOO = true;        // OK

define("Foo", false);    // Noncompliant
//     ^^^^^
define("FOO", true);     // OK

class Bar {
  const Foo = false;     // Noncompliant
  const FOO = true;      // OK
  private $Foo = false;  // OK
}

define();                             // OK
define($not_a_string_literal, false); // OK
