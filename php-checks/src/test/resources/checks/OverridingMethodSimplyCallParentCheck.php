<?php

abstract class B extends A {

  public function f() {      // NOK {{Remove this method "f" to simply inherit it.}}
    parent::f();
  }

  public function f() {      // NOK
    return Parent::f();
  }

  public function g($p1) {   // NOK
    parent::g($p1);
  }

  public function g($p1, $p2, $p3) {   // NOK
    parent::g($p1, $p2, $p3);
  }

  public function g2($p1, $p2, $p3) {   // OK
    parent::g2($p1, $p3, $p2);
  }

  public function e() {      // NOK
    A::e();
  }

  public function h() {      // NOK
    a::h();
  }

  public function h() {      // OK
    return;
  }

  public function i() {      // OK
    parent::i();
    doSomethingElse();
  }

  public function j() {      // OK
  }

  public function k() {      // OK
    function f() {
      parent::i();
    }
  }

   abstract public function l(); // OK

}

class D extends C {
  public $field;

  public function f() {     // OK
    parent::g();
  }

  public function g($p1) {  // OK
    parent::g($p1, 1);
  }

  public function k($p1) {  // OK
    parent::k($p1 + 1);
  }

  public function h() {     // OK
    A::h();
  }

  public function i() {     // OK
    return parent::f() + 1;
  }
}

class E {

  public static function f() {
    return E::f();           // OK
  }

}
