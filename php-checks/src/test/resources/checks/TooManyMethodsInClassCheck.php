<?php

  interface I {             // Noncompliant {{Class "I" has 3 methods, which is greater than 2 authorized. Split it into smaller classes.}}
//^^^^^^^^^

  public function f1();

  public function f2();

  public function f3();
}

  class C1 {       // Noncompliant
//^^^^^

  public function f1() {
  }

  public function f2() {
  }

  private function f3();
}

class C1 {       // OK

  private $i;

  public function f1() {
  }

  public function f2();
}

$x = new class {       // Noncompliant
//       ^^^^^

  public function f1() {  }

  public function f2() {  }

  public function f3() {  }

  private function f4();
};
