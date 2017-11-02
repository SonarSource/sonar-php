<?php

class C {

  public function C() {             // Noncompliant {{Replace this function name "C" with "__construct".}}
//                ^
  }

  public function f() {
  }
}

class C {

  public function C() {             // Noncompliant {{Replace this function name "C", since a "__construct" method has already been defined in this class.}}
  }

  public function __construct() {
  }

  public function f() {
  }
}

class C {

  const C = 1;

  public function __construct() {   // OK
  }

  public function f() {
  }
}


class C {

  public function f() {
  }
}
