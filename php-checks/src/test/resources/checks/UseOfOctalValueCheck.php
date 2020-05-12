<?php

//--------------------------
//  AssignmentExpression
//--------------------------
$a = 1; // Compliant
$a = 123; // Compliant
$a = 1234; // Compliant
$a = 0; // Compliant
$a = 010; // Noncompliant
$a = 012; // Noncompliant
$a = 012; // Noncompliant
$a = 01234; // Noncompliant
$a = "012"; // Compliant
$a = "01234"; // Compliant
$a = 0_1; // Compliant
$a = 0_1234; // Noncompliant

//--------------------------
//  VariableDeclaration
//--------------------------
class Foo {
  public $a = 1; // Compliant
  public $b = 012; // Noncompliant
  public int $c = 1; // Compliant
}

//--------------------------
//  FunctionDeclaration
//--------------------------
function functionTest($default = 012) {} // Noncompliant
function functionTest($param, $default = 012) {} // Noncompliant
function functionTest($param = 1, $default = 012) {} // Noncompliant
function functionTest($default = 1) {} // Compliant
function functionTest($param, $default = 1) {} // Compliant
function functionTest($param = 1, $default = 1) {} // Compliant
function functionTest($default) {} // Compliant
function functionTest() {} // Compliant

//--------------------------
//  FunctionExpression
//--------------------------
callbackTest(function($default = 012) {}); // Noncompliant
callbackTest(function($param, $default = 012) {}); // Noncompliant
callbackTest(function($param = 1, $default = 012) {}); // Noncompliant
callbackTest(function($default = 0) {}); // Compliant
callbackTest(function($param, $default = 0) {}); // Compliant
callbackTest(function($param = 1, $default = 0) {}); // Compliant
callbackTest(function($default) {}); // Compliant
callbackTest(function() {}); // Compliant

//--------------------------
//  ArrowFunctionExpression
//--------------------------
$arrowTest = fn($default = 012) => $x + $y; // Noncompliant
$arrowTest = fn($param, $default = 012) => $x + $y; // Noncompliant
$arrowTest = fn($param = 1, $default = 012) => $x + $y; // Noncompliant
$arrowTest = fn($default = 0) => $x + $y; // Compliant
$arrowTest = fn($param, $default = 0) => $x + $y; // Compliant
$arrowTest = fn($param = 1, $default = 0) => $x + $y; // Compliant
$arrowTest = fn($default) => $x + $y; // Compliant
$arrowTest = fn() => $x + $y; // Compliant

//--------------------------
//  MethodDeclaration
//--------------------------
class Bar {
  public function meth1($default = 1) {} // Compliant
  public function meth2($default = 012) {} // Noncompliant
  public function meth3($default) {} // Compliant
  public function meth4() {} // Compliant
  public static function meth1($default = 1) {} // Compliant
  public static function meth2($default = 012) {} // Noncompliant
  public static function meth3($default) {} // Compliant
  public static function meth4() {} // Compliant
}

//--------------------------
//  Exceptions
//--------------------------
$permissionMask = 0777; // Compliant
$dayOfMonth = 03; // Compliant
