<?php

function f() {           // NOK
  $x = 1;                // OK
}

class A {

  public function f() {  // OK
    $x = 1; // OK
  }
  
  public static $x = 1;  // OK

}

$foo = 1;  // NOK
$foo = 1;  // OK, already reported on previous line
$x->y = 1; // OK
A::x = 1;  // OK
$_GET = 1; // OK
$x[''] = 1;// OK
