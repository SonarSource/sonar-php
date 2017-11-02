<?php

final class A {

  public final function f() {  // Noncompliant {{Remove this "final" modifier.}}
//       ^^^^^
  }

  public function g() {        // OK
  }
  
  public $a = 1;

}

class B {

  public final function f() {  // OK
  }

}
