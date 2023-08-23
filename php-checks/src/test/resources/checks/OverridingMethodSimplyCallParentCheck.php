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

  public function g1($p1, $p2, $p3) {   // Noncompliant
    parent::g1($p1, $p2, $p3);
  }

  public function g2($p1, $p2, $p3) {   // OK
    parent::g2($p1, $p3, $p2);
  }

  public function g3($p1, $p2, $p3) {   // OK
    parent::g3($p1, p3: $p2, p2: $p3);
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

  public function g4() {
    return null;
  }

  public function j() {      // OK
    new class extends B {
      public function f() {      // Noncompliant
        parent::f();
      }

      public function g4() {      // Noncompliant
        B::g4();
      }

    };
  }

  public function k() {      // OK
    function f() {
      parent::i();
    }
  }

   abstract public function l(); // OK

  public function m($arg = 0) { // OK
    parent::m($arg);
  }

  public function n($arg = 0) { // OK
    A::n($arg);
  }

  public function o($arg1, $arg2 = 0) { // OK
    A::o($arg1, $arg2);
  }

  public function p() {
    print("Doesn't reference superclass at all");
  }
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

///////////////////////////////////
// shouldn't trigger if arguments differ

interface FooInterface {}
class FooConcrete implements FooInterface {}

class F
{
    public function __construct(FooInterface $arg1)
    {
        // init..
    }
}

class DefaultF extends F
{
    public function __construct(FooConcrete $arg1)
    {
        parent::__construct($arg1);
    }
}

class MiddleF extends F {}
class LowerF extends MiddleF {
    public function __construct(FooInterface $arg1) // Noncompliant
    {
        parent::__construct($arg1);
    }
}

//////////////////////////////////
// shouldn't trigger if visibility differs

class G
{
    public function __construct()
    {
        // init..
    }
}

class DefaultG extends G
{
    private function __construct()
    {
        parent::__construct();
    }

    public static function factoryMethod(): self {
        // do something
        return new self();
    }
}

class AnotherG extends G
{
    public function __construct($arg)
    {
        parent::__construct($arg);
    }
}
