<?php

/**
 *  BASIC VARIABLE
 */
function f() {
  $a = 1;    // OK
  $b = 1;    // OK
  $c = 1;    // NOK
  $d =& $a;  // NOK
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
$a = function($p) use ($u) {   // OK - $u exclusion
	return $p;
};

function j($p) {
  $a = 1;                      // OK - use in anonymous function
  $b = 1;                      // OK - use in anonymous function

  call(function () use ($a, $b, $p) {  // NOK - $b ($p exclusion)
    return $a;
  });
}

/*
 *  USE WITH REFERENCE
 */
function j() {
  $a = 1;                                 // OK - use in anonymous function

  call(function () use (&$a, &$b, &$c) {  // NOK - $a, $c (not use in outer, reference not needed )
    $b = 1;
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
    return function (FormView $view) use ($a) {       // OK used
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
  list(list($c) = array()) = array(); // NOK - $c

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

/**
 *  redefine
 */
function q() {
  $i = 0;
  echo ++$i;
  echo ++$i;        // OK

  $j = 1;
  echo $j++;
  echo $j++;        // NOK

  $a = 'a';
  $a .= 'a';        // NOK

  $b = 'b';
  $b = $b.'b';      // NOK
}

/**
 *  FOREACH
 */
function r() {
  $arr = array(0, 0);               // OK

  foreach ($arr as $v1) {           // OK
    echo $v1;
  }

  foreach ($arr as $k2 => $v2) {    // OK
    echo $k2.': '.$v2;
  }

  foreach ($arr as &$v3) {          // OK
    $v3 = 3;
  }
  foreach ($arr as $k4 => &$v4) {   // OK
    echo $k4;
    $v4 = 4;
  }

  foreach ($arr as $v5) {           // NOK $v5
  }

  foreach ($arr as $v6) {
    $v6 = 6;                        // NOK $v6
  }

  foreach ($arr as &$v7) {          // NOK $v7
  }

  foreach ($arr as $k8 => $v8) {    // NOK $v8
    echo $k8;
  }

  foreach ($arr as $k9 => $v9) {    // NOK $k9
    echo $v9;
  }

  foreach ($arr as $k10 => $v10) {
    echo $k10;
    $v10 = 10;                      // NOK $v10
  }

  foreach ($arr as $k11 => $v11) {
    $k11 = 11;                      // NOK $k11
    echo $v11;
  }

  foreach ($arr as $k12 => &$v12) { // NOK $v12
    echo $k12;
  }

  foreach ($arr as $k13 => &$v13) { // NOK $k13
    echo $v13;
  }

  foreach ($arr as $k14 => &$v14) {
    $k14 = 14;                      // NOK $k14
    echo $v14;
  }

  return $arr;
}

/**
 *  loops
 */
function s() {
  $a = true;
  $b = 0;
  $c = 0;
  while ($a) {
    $a = false;                     // OK
    echo $b++;                      // OK
    $c++;                           // NOK
  }

  for ($i = 0; $i < 10; $i++) {
    $i = 12;                        // OK
  }

  for ($j = 0; $j < 10; ) {
    $j++;                           // OK
  }

  $arr = array(0, 1);
  $done = false;
  foreach ($arr as $v) {
    if ($done) {
      return;
    }
    echo $v;
    $done = true;                   // OK
  }

}

/**
 *  objects
 */
function r() {

  $object1 = new stdClass();
  $object2 = new stdClass();

  foreach ($list as $item) {
    $item->field = true;            // OK
    $object1->field = 1;            // OK
  }

  $object2 = 2;                     // NOK

}

class C {

  private static $a;

  function f1() {
    self::$a++;                     // OK
  }
}