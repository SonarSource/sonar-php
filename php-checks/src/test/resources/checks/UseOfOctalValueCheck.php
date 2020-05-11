<?php

//--------------------------
//  AssignmentExpression
//--------------------------
$a = 1; // Compliant
$a = 123; // Compliant
$a = 0; // Compliant
$a = 00; // Noncompliant
$a = 0010; // Noncompliant
$a = 0123; // Noncompliant
$a = 0123; // Noncompliant
$a = "0123"; // Compliant

//--------------------------
//  VariableDeclaration
//--------------------------
class Foo {
  public $a = 1; // Compliant
  public $b = 0123; // Noncompliant
  public int $c = 1; // Compliant
}

//--------------------------
//  FunctionDeclaration
//--------------------------
function functionTest($default = 0123) {} // Noncompliant
function functionTest($param, $default = 0123) {} // Noncompliant
function functionTest($param = 1, $default = 0123) {} // Noncompliant
function functionTest($default = 1) {} // Compliant
function functionTest($param, $default = 1) {} // Compliant
function functionTest($param = 1, $default = 1) {} // Compliant
function functionTest($default) {} // Compliant
function functionTest() {} // Compliant

//--------------------------
//  FunctionExpression
//--------------------------
callbackTest(function($default = 0123) {}); // Noncompliant
callbackTest(function($param, $default = 0123) {}); // Noncompliant
callbackTest(function($param = 1, $default = 0123) {}); // Noncompliant
callbackTest(function($default = 0) {}); // Compliant
callbackTest(function($param, $default = 0) {}); // Compliant
callbackTest(function($param = 1, $default = 0) {}); // Compliant
callbackTest(function($default) {}); // Compliant
callbackTest(function() {}); // Compliant

//--------------------------
//  ArrowFunctionExpression
//--------------------------
$arrowTest = fn($default = 0123) => $x + $y; // Noncompliant
$arrowTest = fn($param, $default = 0123) => $x + $y; // Noncompliant
$arrowTest = fn($param = 1, $default = 0123) => $x + $y; // Noncompliant
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
  public function meth2($default = 0123) {} // Noncompliant
  public function meth3($default) {} // Compliant
  public function meth4() {} // Compliant
  public static function meth1($default = 1) {} // Compliant
  public static function meth2($default = 0123) {} // Noncompliant
  public static function meth3($default) {} // Compliant
  public static function meth4() {} // Compliant
}
