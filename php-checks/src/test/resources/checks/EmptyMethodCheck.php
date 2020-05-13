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

  public function method5() {
    /* Comment */
  } // Compliant

  public function method6() {
    /** Comment */
  } // Compliant

  public function method7() { /** Comment */ } // Compliant

  public function method8() { /**/ } // Noncompliant

  public function method9() {throw new Exception();} // Compliant
}

abstract class AbstractClass {
  public function defaultMethod1() { } // Compliant

  abstract function abstractMethod1();
}

final class FinalClass {
  public function defaultMethod1() { } // Noncompliant
}

trait AnonymousClassInTrait {
    public function test() {
        return new class() {
            public function foo() {} // Noncompliant
        };
    }
}
