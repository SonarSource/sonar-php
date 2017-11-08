<?php

$a = 0;

function foo() {
  $a = 1;
  global $a;
  $a = 2;
  static $a = 3;
}
