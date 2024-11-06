<?php

namespace NS;

#[A1]
class A extends B {
  public $field; /* comment 1 */ // comment 2

  public function foo(#[A2, A3,] int|array $a, int|(A&B) $b) {
    $var = 1;
    foo(1, 2);
    `cat $var`;
    match ($a) {1=>1};
    match ($a) {default=>1};
    f(...);
  }

  public function bar(callable&iterable $b) {
    return $b;
  }
}

enum A {
  case A;
  case B;
}
