<?php

interface I {             // NOK

  public function f1();

  public function f2();

  public function f3();
}

abstract class C1 {       // NOK

  public function f1() {
  }

  public function f2() {
  }

  private function f3();
}

abstract class C1 {       // OK

  private $i;

  public function f1() {
  }

  public function f2();
}
