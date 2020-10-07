<?php

namespace NS;

class A extends B {
  public $field; /* comment 1 */ // comment 2

  public function foo(int|array $a) {
    $var = 1;
    foo(1, 2);
    `cat $var`;
  }
}
