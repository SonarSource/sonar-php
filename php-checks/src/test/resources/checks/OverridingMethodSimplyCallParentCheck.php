<?php

class B extends A {

  public function f() {      // NOK
    parent::f();
  }

  public function f() {      // NOK
    return parent::f();
  }

  public function g($p1) {   // NOK
    parent::g($p1);
  }

  public function g($p1, $p2, $p3) {   // NOK
    parent::g($p1, $p2, $p3);
  }

  public function h() {      // NOK
    A::h();
  }

}

class D extends C {

  public function f() {     // OK
    parent::g();
  }

  public function g($p1) {  // OK
    parent::g($p1, 1);
  }

  public function h() {     // OK
    A::h();
  }

  public function i() {     // OK
    return parent::f() + 1;
  }
}
