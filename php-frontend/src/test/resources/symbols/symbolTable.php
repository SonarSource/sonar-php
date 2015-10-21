<?php

$a = 1;                                       // $a

const CONSTANT = 1;                          // $constant

list($l1, $l2) = $unassigned;                 // $l1, $l2, $unassigned

foreach (array("a", "b") as $key => $val) {}  // $key, $val

function f() {                                // f
  static $static;                             // $static
  $a = 1;                                     // $a
}

class A {                                     // A
  public $field1, $field2 = 1;                // $field1, $field2
  const CONSTANT_FIELD;                       // $constantField

  public function f($p) {                     // f, $p
  }
}

