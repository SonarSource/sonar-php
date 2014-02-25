<?php

class B extends A {

  public function __construct() {
    parent::A();                   // NOK
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

class C extends A {

  public function __construct() {
    parent::__construct();         // OK
  }
}
