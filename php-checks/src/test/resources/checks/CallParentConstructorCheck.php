<?php

class B extends A {
  public function __construct() {
    parent::A();                   // Noncompliant {{Replace "parent::A(...)" by "parent::__construct(...)".}}
//  ^^^^^^^^^^^
    parent::f();                   // OK
    parent::FOO->f();              // OK
  }
}

$x = new class extends A {
  public function __construct() {
    parent::A();                   // Noncompliant
  }
};

$y = new class  {
  public function __construct() {  }
};

class C extends A {
  public function C() {
    parent::A();                   // OK
  }
}

class D extends A {
  public function __construct() {
    parent::__construct();         // OK
    parent->__construct();         // OK - syntactically not correct accordingly to PHP interpreter but accepted by the parser
    Z::f();
  }
}

class E {
  public function __construct() {
  }
}

class F extends A {
  public function __construct() {
  }
}

class G  {
  public function __construct() {
    parent::Name();                 // OK - syntactically not correct accordingly to PHP interpreter but accepted by the parser
  }
}

class H extends A {
   public function f() {
     parent::A();                   // OK - not in scope
   }
}

interface I2 extends I1 {
  function f();
}

parent::A();
