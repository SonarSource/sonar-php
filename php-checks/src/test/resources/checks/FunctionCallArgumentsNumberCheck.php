<?php

namespace Checks\FunctionCallArgumentsNumberCheck;

  function function1($a, $b) {}
//         ^^^^^^^^^> {{Function definition.}}
  function1($a); // Noncompliant {{"function1" expects 2 arguments, but 1 was provided. Add more arguments or define default values.}}
//^^^^^^^^^

function function2($a) {}
function2($a, $b); // Noncompliant {{"function2" expects 1 argument, but 2 were provided. Reduce provided arguments or add more parameters.}}

function function3($a, $b = null) {}
function3($a); // OK

function function4(...$a) {}
function4($a, $b, $c); // OK

function function5() {}
function5($a, $b, $c); // Noncompliant {{"function5" expects 0 arguments, but 3 were provided. Reduce provided arguments or add more parameters.}}

function function6() {$args = func_get_args();}
function6($a); // OK

function function7() {
  $subFunction = function() {$args = func_get_args();};
}
function7($a); // Noncompliant

function function8() {
  $arg = func_get_arg(1);
}
function8($a); // OK

namespace Foo {
  function namespaced($a) {}
}
Foo\namespaced(); // Noncompliant

// Code Coverage
$bar->foo();
new Foo($a);

function function9($a, ...$args) {
  function1(...$args); // OK - spread argument
}
function9($a); // OK
function9($a, $b); // OK
function9(); // Noncompliant {{"function9" expects 1 argument, but 0 were provided. Add more arguments or define default values.}}

function function10($a = new A()) {}
function10(); // Compliant

function function11($a, ...$args) {
  function1(...$args); // OK
}
function11(1);
