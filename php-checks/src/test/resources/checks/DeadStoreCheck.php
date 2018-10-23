<?php

function foo($a) {}


/***************************
 * GLOBAL SCOPE
 ***************************/

$a = 1;

function global_scope_not_local() {
  $a = 42; // has the same name as the global $a, but is in local scope
  foo($a);
}

function globalScope() {
  global $a;
  $a = 2; // compliant, we ignore global scope
  $a = 3;
}

/***************************
 * PARAMETERS
 ***************************/

function param_are_ignored($two, $three) {
  $two = 42;
  $two = 43;
  foo($two);
}

/***************************
 * ASSIGNMENTS
 ***************************/

function simple() {
  $one = 42; // Noncompliant {{Remove this useless assignment to local variable '$one'.}}
  $one = 3; // Noncompliant
//^^^^^^^^^
  $one = 4;
  foo($one);

  $two = 12;
  foo($two);

  $three = 42; // this gets reported by S1481 - unused local variable
}

function increment() {
  $x = 42;
  foo($x);
  $x++; // both read and write
  $y = 31;
  ++$y;
  $z = 32;
  $z--;
  $w = 33;
  --$w;
}

function minus() {
  $x = 42;
  $y = -$x;
  return $x;
}

function assign_in_method_call() {
  $result = 100;
  foo($result = 200);
}

function chain_assign() {
  $a = $b = 0; // both are initialized to basic value
  $a = foo();
  $b = 42;
  foo($a, $b);
}

function compound_assignments() {
  $fTerm = 100;
  $fTerm *= foo();
  $fTerm /= foo();
  $fTerm -= foo();
  $fTerm += foo();
  $fTerm .= foo();
  return $fTerm; // OK
}

function array_assignment(){
  list($a, $b) = foo();  // reported by S1481 - unused local variable

  list($c, $d) = foo();
  foo($c);
  foo($d);
  // Noncompliant@+1
  list($c, $d) = foo(); // Noncompliant
}

function assignment_in_lhs() {
  $a = 43; // FN - we do not consider order inside expressions
  $b[$a = foo()] = bar($a);
}

/***************************
 * DEFAULT VALUES
 ***************************/

function basic_values() {
  $first = 0.0;
  $first = 42;
  foo($first);

  $one = -1;
  $one = 0;
  $one = 1;
  $one = 31;
  foo($one);

  $two = "";
  $two = "foo";
  foo($two);

  $six = '';
  $six = bar();
  foo($six);

  $three = false;
  $three = FALSE;
  $three = bar();
  foo($three);

  $four = true;
  $four = TRUE;
  $four = bar();
  foo($four);

  $five = null;
  $five = NULL;
  $five = bar();
  foo($five);

  $seven = [];
  $seven = ["one", "two"];
  foo($seven);

  $eight = array();
  $eight = array("one", "two");
  foo($eight);

  // resetting existing value to default values is ok
  $one = 0;
  $two = -1;
  $three = "";
  $four = '';
  $five = NULL;
  $six = [];
  $seven = array();
  $eight = false;
}

function almost_basic_value() {
  $seven = "  "; // Noncompliant
  $seven = bar();
  foo($seven);

  $eight = '  '; // Noncompliant
  $eight = bar();
  foo($eight);

  $nine = 1.0; // Noncompliant
  $nine = 1;
  foo($nine);

  $ten = -2; // Noncompliant
  $ten = 42;
  foo($ten);
}

function hex_zero() {
  $length = 0x00000; // Noncompliant
  $length = foo();
  return $length;
}

class FooBar {
  const CONSTANT = 'constant value';
  function init_with_constant() {
    $x = self::CONSTANT; // Noncompliant
    $x = 100;
    foo($x);
  }
}

/***************************
 * IF , SWITCH
 ***************************/

function ifs_noncompliant() {
  $one = 10; // Noncompliant
  if (condition) {
    $one = 11;
    foo($one);
  }

  $two = 12; // is used inside if
  if (condition) {
    foo($two);
  } else {
    $two = 13; // Noncompliant
  }

  $three = 14; // Noncompliant
  if (condition) {
    stmt();
  }
  $three = 15;
  foo($three);

}

function ifs_compliant() {
  $one = 3; // is used inside if
  if (condition) {
    foo($one);
    $one = 4; // is used after if
  } else {
    $one = 5; // is used after if
  }
  foo($one);

  $two = 6; // is used after if
  if (condition) {
    $two = 7;
  }
  foo($two);

  $three = 5; // is used inside if
  if (condition) {
    foo($three);
  }

  $four = 6; // is used inside nested if
  if (condition) {
    stmt();
    if (condition2) {
      foo($four);
    }
  }
}

function switch_with_default() {
  $x = defaultValue(); // Noncompliant
  switch (something) {
    case 1:
      $x = 101;
      break;
    case 2:
      $x = 102;
      break;
    default:
      $x = defaultValue();
  }
  return $x;
}

/***************************
 * LOOPS
 ***************************/

function loops_noncompliant() {
  $one = 42; // Noncompliant
  while (condition) {
    $one = bar();
    foo($one);
  }

  $two = 0; // default value
  foo($two);
  while (condition) {
    $two = bar(); // Noncompliant
  }

  $three = 0;
  foo($three);
  do {
    $three = 42; // Noncompliant
  } while (condition);

  for ($four = 0, $five = 42; $four <= 10; $four++) { // Noncompliant {{Remove this useless assignment to local variable '$five'.}}
//                ^^^^^^^^^^
    $five = 134;
    foo($five);
  }

  for ($six = 0, $seven = 42; $six <= 10; $six++) { // $seven gets reported by S1481 - unused local variable
    stmt();
  }
}

function loops_compliant() {
  $one = 42;
  while($one < 10) {
    $one = $one + 1;
  }
  foo($one);

  $two = 0; // default value
  while (condition) {
    $two = $two + 1;
    foo($two);
  }

  $three = 0;
  while(condition) {
    $three = $three + 1; // read and write
  }

  $three = 0;
  do {
    $three = 42;
  } while (condition);
  foo($three);

  for ($four = 0; $four <= 10; $four++) {
    stmt();
  }

  foreach ($arr as $key => $value) {
    $value = 42;
    foo($value);
  }

  foreach ($arr as $key => $value) {
    stmt();
  }
}

function do_while_with_continue() {
  $p = 0;
  do {
    if (condition) {
      $p = bar();
      continue;
    }
  } while (!$p);
}

/***************************
 * NESTED FUNCTIONS
 ***************************/

function nested_function_has_different_scope() {
  function innerNoParam() {
    $a = 22;
    foo($a);
  }
  $a = 42; // reported by S1481

  $b = 42;
  innerNoParam($b);
}

function anonymous_lambda_use($kernel)
{
    // false positive because $a is considered a different symbol inside the 'use',
    // even though it's the same symbol
    $a = 42; // Noncompliant

    $foo->method(function ($x) use ($a) { // we don't realize $a is the same here
      return $a[$x];
    });

    $a = 42;
    $foo->method(bar($a)); // OK
}


/***************************
 * TRY, CATCH, FINALLY
 ***************************/

function catch_exception() {
  try {
    doSomething();
  } catch (Exception $e) {    // OK, ignore parameter
    return false;
  }
}

function usage_in_catch() {
  $foo = 42;
  foo($foo);
  $foo = 43;
  try {
    bar();
  } catch (Exception $e) {
    report($foo); // ok
  }
}

function no_usage_in_catch() {
  $foo = 42;
  foo($foo);
  $foo = 43; // Noncompliant
  try {
    bar();
  } catch (Exception $e) {
    report();
  }
}

function usage_in_finally() {
  $foo = 42;
  foo($foo);
  $foo = 43;
  try {
    bar();
  } finally {
    report($foo); // ok
  }
}

function usage_in_finally_in_loop() {
  $foo = 42;
  foo($foo);
  $foo = 43;
  while(cond) {
    try {
      throw new Exception();
    } finally {
      report($foo); // ok
    }
  }
}

/***************************
 * MISC
 ***************************/

function return_array() {
  $realNumber = 0;
  $realNumber = foo();
  return array('real' => $realNumber); // OK
}

function param_by_value(&$cellValue) {
  $cellValue = foo();
}

