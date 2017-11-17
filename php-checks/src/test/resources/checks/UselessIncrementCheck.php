<?php

$a = 42;
$b = 0;

class A {
  private $a = 42;

  public function foo() {
    $this->$a = -1;
    return $this->$a++;
  }

  public function bar() {
    $a = 0;
    return $a++; // Noncompliant
  }
}

function pickNumber() {
  global $b;
  static $k = 0;

  $i = 0;
  $j = 0;

  $j = $j--; // Noncompliant {{Remove this decrement or correct the code not to waste it.}}
  $i = $i++; // Noncompliant {{Remove this increment or correct the code not to waste it.}}
//     ^^^^

  $j += $j++; // Compliant
  $i = ++$i;  // Compliant
  $j = $i++;  // Compliant

  if ($j < 42) {
    return ($j)--; // Noncompliant
//         ^^^^^^
  } else if ($i > 42) {
    return $a[$j]++; // Compliant not a variable directly incremented
  } else if ($a > 42) {
    return $b++; // Compliant - variable is global
  } else if ($k > 42) {
    return $k++; // Compliant - variable is static
  } else {
    return;
  }
}

function counter($start) {
  $value = $start;

  return function() use (&$value) {
    $c = 0;
    if ($value > 6) {
      return $c++; // Compliant: FN - skipping function expressions
    }
    return $value++; // Compliant: Would be FP if considering function expressions
  };
}

function other() {
  for ($i = 1; $i <= 10; $i++) { // Compliant
    echo $i++;
    if ($i > 42) {
      return 42;
    }
  }
  return $a++; // Compliant: $a is defined in outer scope
}

return $a--; // Compliant: only considering variables from functions
