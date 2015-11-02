<?php

namespace NS;

class A extends B {
  public $field; /* comment 1 */ // comment 2

  public function foo() {
    $var = 1;
    foo(1, 2);
  }
}
