<?php

class A {

  private function __construct() {  // OK - private constructor
  }

  private function f() {            // Noncompliant {{Remove this unused private "f" method.}}
//                 ^
    $foo = clone $this;

    $code = '_i';
    echo "${foo->j()}";
    return $foo->g(1)
               ->h();
  }

  private function g($p1) {         // OK
  }

  private function h() {            // OK
  }

  private function _i() {           // OK - used in a simple string literal
  }

  private function j() {            // OK - used as encapsulated variable in string
  }

  public function k() {             // OK - public
  }

  public function __i() {           // OK - magic method
  }

  function m() {                    // OK - default (=> not private)
  }

  static private function n() {     // Noncompliant
  }

}

class B {

  private $field;

  private function B() {              // OK - private constructor
  }


}


/**
 *  SONARPHP-402
 */
class HeredocUsage {
  private function foo() {}

  function heredoc_usage() {
    echo <<<EOF
    {$this->foo()}
EOF;
  }
}

$x = new class {

  private function __construct() {  // OK - private constructor
  }

  private function f() {            // Noncompliant {{Remove this unused private "f" method.}}
//                 ^
    $this->h();

  }

  private function h() {            // OK, used
  }

  public function k() {             // OK - public
  }

  function m() {                    // OK - default (=> not private)
  }

  static private function n() {     // Noncompliant
  }
};
