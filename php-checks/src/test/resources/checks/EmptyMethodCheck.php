<?php

class TestClass {
  public function method1() { } // Noncompliant

  public function method2() {
    echo 1;
  } // Compliant

  public function method3() {
    // Comment
  } // Compliant

  public function method4() {
    # Comment
  } // Compliant

  public function method5() { /** Comment */ } // Compliant

  public function method6() {throw new Exception();} // Compliant
}

abstract class AbstractClass {
  public function defaultMethod1() { } // Compliant

  abstract function abstractMethod1();
}
