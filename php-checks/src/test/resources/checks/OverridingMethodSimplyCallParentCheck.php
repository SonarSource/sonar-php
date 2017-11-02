<?php

abstract class B extends A {

  public function f() {      // Noncompliant {{Remove this method "f" to simply inherit it.}}
//                ^
    parent::f();
  }

  public function f() {      // Noncompliant
    return Parent::f();
  }

  public function g($p1) {   // Noncompliant
    parent::g($p1);
  }

  public function g($p1, $p2, $p3) {   // Noncompliant
    parent::g($p1, $p2, $p3);
  }

  public function g2($p1, $p2, $p3) {   // OK
    parent::g2($p1, $p3, $p2);
  }

  public function e() {      // Noncompliant
    A::e();
  }

  public function h() {      // Noncompliant
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
    new class extends B {
      public function f() {      // Noncompliant
        parent::f();
      }

      public function g() {      // Noncompliant
        B::g();
      }

    };
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
