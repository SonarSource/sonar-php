<?php

/**
 *  BASIC VARIABLE
 */
function f() {
  $a = 1;    // OK
  $b = 1;    // OK
  $c = 1;    // NOK {{Remove this unused "$c" local variable.}}
  $d =& $a;  // NOK {{Remove this unused "$d" local variable.}}
  $a[$b];
}

/**
 *  GLOBAL
 */
function g($p) {
  global $a;

  $a = 1;         // OK - global var
  $b = 1;         // NOK
  $c = $p->a;     // OK

  return $c;
}

/**
 *  STATIC
 */
function h() {
  static $a, $b = 1;  // NOK - $b

  return $a;
}

/**
 *  PARAMETER
 */
function i($p) {  // OK - not a local variable
   $a = 1;        // NOK

   return $p;
}

/*
 *  USE
 */
$a = function($p) use ($u) {   // OK - $u exclusion as it's not defined
	return $p;
};

function j($p) {               // OK
  $a = 1;                      // OK - use in anonymous function
  $b = 1;                      // OK - use in anonymous function

  call(function () use ($a, $b,   // NOK
                            $p) { // NOK
    return $a;
  });
}

/*
 *  USE WITH REFERENCE
 */
function j() {
  $a = 1;                                 // OK - use in anonymous function

  call(function () use (&$a,             // NOK - $a (not use in outer, reference not needed )
                            &$b, &$c) {  // NOK - $c (not use in outer, reference not needed )
    $b = 1 ;
    $c = 1;
  });

  return $b;
}

/**
 * SUPER-GLOBALS
 */
function k() {
  $GLOBALS["name"] = "name";  // OK
  $_POST["name"] = "name";    // OK
}

/**
 * NEW EXPRESSION
 */
function l() {
  $class = "MyClass";   // OK

  return new $class ();
}

/**
 *  $this
 */
class C {
  static $a = 1;
  private $b = 1;

  public function f() {
    self::$a = 1;       // OK
    $this->b = 1;       // OK

  }
}

/**
 * SCOPE
 */
function m($p){

  $a = $p;                                            // OK - use in anonymous function
  $b = function () use ($a) {                         // OK - use in anonymous function
          return function (FormView $view) use ($a) { // OK used
          $a->do();
        };
     };

  doSomething($b);
}

/**
 * LIST
 */
function n(){

  list($a, $b) = array();             // NOK - $a
  list(static::$d) = array();         // OK
  list(list($c)) = array();           // NOK {{Remove this unused "$c" local variable.}}

  doSomething($b);
}

/**
 * VARIABLE VARIABLES
 */
function o(){
  global $a, $$a;  // OK variable variables are not supported

  return $a;
}

function m($p){
  $a = "property";  // OK

  return $p->$a;
}

/**
 *  VARIABLE VARIABLES
 */
function o(){
  foreach ($arr as &$v) {
    $v = 3;
  }

  foreach ($arr as $key1 => $value1) { // OK - cannot have the key without defining the value
      $key1 = 3;
  }

  foreach ($arr as $key2 => &$value2) { // OK - cannot have the key without defining the value
      $key2 = 3;
  }
}

/**
 * ENCAPSULATE VARIABLE
 */
function p(){
  $a1 = 1; $a2 = 1; $a3 = 1;    // OK
  $b1 = 2; $b2 = 2; $b3 = 2;    // OK
  $c1 = 3;                      // OK

  echo "Simple encaps  $a1";
  echo "Semi-complex encaps  ${a2}";
  echo "Complex encaps  {$a3}";

  echo "Simple encaps  $b1->c";
  echo "Semi-complex encaps  ${b2[0]}";
  echo "Complex encaps  {$b3 + $c1}";
}

/**
 * OUT OF EVERY SCOPE
 */
$a = 1;

function nested_foreach() {
  foreach([1, 2] as $k){
    echo $k;
    foreach([1, 2] as $rowKey => $cellValue) {
      echo $rowKey;
    }
  }
}


function arguments_are_ignored() {
  sscanf("somestr", "somepattern", $arg1);     // OK
  foo($arg2);                                  // OK
}

function catch_exception() {
  try {
    doSomething();
  } catch (\InvalidArgumentException $e) {    // OK
    return false;
  }

  ItemQuery::$result1 = 1;
  ItemQuery->result4 = 1;
  this::$result2 = 1;
  this->result3 = 1;
}

function foreach_key_declared_twice($arr) {

  foreach([1, 2] as $key => $val) {    // Should be NOK (FN, SONARPHP-587)
    echo "hello";
  }

  foreach($arr as $key => $value) {    // Should be NOK (FN, SONARPHP-587)
    echo "world";
  }
}
