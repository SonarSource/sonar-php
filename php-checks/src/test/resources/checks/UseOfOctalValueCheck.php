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
$a = 020000000000; // Noncompliant

//--------------------------
//  VariableDeclaration
//--------------------------
class Foo {
  public $a = 1; // Compliant
  public $b = 012; // Noncompliant
}

//--------------------------
//  FunctionDeclaration
//--------------------------
function functionTest($default = 012) {} // Noncompliant
function functionTest($default = 1) {} // Compliant

//--------------------------
//  FunctionExpression
//--------------------------
callbackTest(function($default = 012) {}); // Noncompliant
callbackTest(function($default = 0) {}); // Compliant

//--------------------------
//  ArrowFunctionExpression
//--------------------------
$arrowTest = fn($default = 012) => $x + $y; // Noncompliant
$arrowTest = fn($default = 0) => $x + $y; // Compliant

//--------------------------
//  MethodDeclaration
//--------------------------
class Bar {
  public function meth1($default = 1) {} // Compliant
  public function meth2($default = 012) {} // Noncompliant
}

//--------------------------
//  Exceptions
//--------------------------
$permissionMask = 0777; // Compliant
$dayOfMonth = 03; // Compliant
