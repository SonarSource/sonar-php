<?php

function f() {           // Noncompliant {{Move this function into a class.}}
//       ^
  $x = 1;                // OK
  function nested() {}   // OK
}

foo(function () {
  $y = 1;                // OK
  function nested() {}   // OK
});

class A {

  public function f() {  // OK
    $x = 1; // OK
    function nested() {} // OK
  }
  
  public static $x = 1;  // OK

}

  $foo = 1;  // Noncompliant {{Move this variable into a class.}}
//^^^^
$foo = 1;  // OK, already reported on previous line
$x->y = 1; // OK
A::x = 1;  // OK
$_GET = 1; // OK
$x[''] = 1;// OK
