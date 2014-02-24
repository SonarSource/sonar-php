<?php

$myObject->define();     // OK
$myObject->define(true); // OK
call("CONST");           // OK

const Foo = false;       // NOK
const FOO = true;        // OK

define("Foo", false);    // NOK
define("FOO", true);     // OK

class Bar {
  const Foo = false;     // NOK
  const FOO = true;      // OK
}
