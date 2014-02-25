<?php

class B extends A {

  public function __construct() {
    parent::A();                   // NOK
    parent::f();                   // OK
  }

 public function f() {
    parent::A();                   // OK - not in scope
 }
}

class C extends A {

  public function C() {
    parent::A();                   // OK
  }
}

class D extends A {

  public function __construct() {
    parent::__construct();         // OK
    Z::f();
  }
}

class E {

  public function __construct() {
  }
}
