<?php

/**
 *  BASIC VARIABLE
 */
function f() {
  $a = 1;    // OK
  $A = 1;    // OK
  $b = 1;    // OK
  $B = 1;    // Noncompliant {{Remove this unused "$B" local variable.}}
//^^
  $c = 1;    // Noncompliant {{Remove this unused "$c" local variable.}}
//^^
  $d =& $a;  // Noncompliant {{Remove this unused "$d" local variable.}}
  $a[$b];
  echo $A;
}

/**
 *  GLOBAL
 */
function g($p) {
  global $a;

  $a = 1;         // OK - global var
  $b = 1;         // Noncompliant
  $c = $p->a;     // OK

  return $c;
}

/**
 *  STATIC
 */
function h() {
  static $a, $b = 1;  /* Noncompliant */ // $b
//           ^^
  static $c; // Noncompliant

  return $a;
}

/**
 *  PARAMETER
 */
function i($p) {  // OK - not a local variable
   $a = 1;        // Noncompliant

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
  $b = 1;                      // Noncompliant

  call(function () use ($a, $b,   // Noncompliant
//                          ^^
                            $p) { // Noncompliant
    return $a;
  });
}

/*
 *  USE WITH REFERENCE
 */
function j() {
  $a = 1;                       // Noncompliant

  call(function () use (&$a,    /* Noncompliant */ // $a (not use in outer, reference not needed )
                        &$b,
                        &$c) {  /* Noncompliant */ // $c (not use in outer, reference not needed )
    $b = 1 ;
    $c = 1;
  });

  return $b;
}

function j2() {
  $a = 1;

  $foo = function () use (&$a) {
    echo $a;
  };
  $foo();
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

  list($a, $b) = array();             /* Noncompliant */ // $a
  list(static::$d) = array();         // OK
  list(list($c)) = array();           // Noncompliant {{Remove this unused "$c" local variable.}}
//          ^^

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
      foo($key1);
  }

  foreach ($arr as $key2 => &$value2) { // OK - cannot have the key without defining the value
      foo($key2);
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

  foreach([1, 2] as $key => $val) {    // Noncompliant
//                  ^^^^
    echo "hello";
  }

  foreach($arr as $key => $value) {    // Should be NOK (FN, SONARPHP-587)
    echo "world";
  }
}


/**
 *  SONARPHP-602
 */
class Foo{
  public function foo()
  {
     ObjectCustomService::class;
     $M = 1;
     return $M; // OK
  }
}

/**
 *  SONARPHP-402
 */
function heredoc_usage() {
   $a = 1;
   echo <<<EOF
   {$a}
EOF;

}

function compact_function() {
  $a = foo();
  $b = bar();
  return compact("a", "b");
}

function compact_function_with_backslash() {
  $a = foo();
  return \compact("a");
}

function compact_function_with_array() {
    $city  = "San Francisco"; // Compliant
    $location_vars = array("city");
    $result = compact($location_vars);
    var_dump($result);
}

class A {

    function foo() {
        $unused = 1; // Compliant
        $vars = 'unused';
        compact($vars);
    }

    function bar() {
        $unused = 1; // Noncompliant
    }
}


function extract_function() {
  $var_array = array("color" => "blue");
  extract($var_array);
  echo "$color";
}

/**
 * SONARPHP-760
 */
function anonymous_class() {

  $foo760 = "..."; // used as anonymous class argument
  $bar760 = "..."; // used as anonymous class argument
  $qux760 = "..."; // used as function call argument
  $qix760 = "..."; // Noncompliant

  new class($foo760) {
    private $bar;
    public function __construct($bar) { $this->bar = $bar; }
  };

  new class($bar760) { };
  new class($unknown760) { };

  new class(strtolower($qux760)) {
    public $b;
    public function __construct($b) { $this->b = $b; }
  };
}

function nested_compound_assignment_is_read() {
  $x2 = 42; // Noncompliant
  foo($x2 = 0);

  $x3 = $x4 = 3; // Noncompliant
//      ^^^
  foo($x3);

  $x5 = 42; // Noncompliant
  $x5 = 43;

  $x6 = 42; // Noncompliant
  $x6 += 43;

  $x1 = 42;
  foo($x1 += 0);

  $x7 = 42;
  return $x7 += 0;
}

function simple_for() {
  for ($six = 0, $seven = 42; $six <= 10; $six++) { // Noncompliant
//               ^^^^^^
    stmt();
  }
}
