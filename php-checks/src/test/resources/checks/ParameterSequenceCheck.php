<?php

//-----------------
// Function Calls
//-----------------
// function testFunction($a, $b = null) { }
//
// testFunction($a, $b); // Compliant
// testFunction($b, $a); // Noncompliant
// testFunction($a, $a); // Compliant
//
// testFunction($c, $d); // Compliant
// testFunction($a, $e); // Compliant
// testFunction($f, $b); // Compliant
// testFunction($a); // Compliant
// otherFunction($b, $a); // Compliant


//-----------------
// Self Method Calls
//-----------------
class TestClass {
  public static function testMethod($a, $b = null) { }

//   public static function otherTestMethod() {
//     $this->testMethod($a, $b); // Compliant
//     $this->testMethod($b, $a); // Noncompliant
//     $this->testMethod($c, $d); // Compliant
//     $this->testMethod($a, $e); // Compliant
//     $this->testMethod($f, $b); // Compliant
//     $this->testMethod($a); // Compliant
//
//     self::testMethod($a, $b); // Compliant
//     self::testMethod($b, $a); // Noncompliant
//   }
}

//-----------------
// Extended Method Calls
//-----------------
class ChildClass extends TestClass {
  public static function otherTestMethod() {
      $this->testMethod($a, $b); // Compliant
      $this->testMethod($b, $a); // Noncompliant
    }
}

//-----------------
// Dynamic Method Calls
//-----------------
// $testClass = new TestClass();
// $testClass->testMethod($a, $b); // Compliant
// $testClass->testMethod($f, $b); // Compliant
// $testClass->testMethod($b, $a); // Noncompliant
// $otherClass->testMethod($b, $a); // Compliant

//-----------------
// Static Method Calls
//-----------------
// TestClass::testMethod($a, $b); // Compliant
// TestClass::testMethod($f, $b); // Compliant
// TestClass::testMethod($b, $a); // Noncompliant
// OtherClass::testMethod($b, $a); // Compliant

//-----------------
// Code Coverage
//-----------------
// function functionWithoutParameters() {}
// functionWithoutParameters();
// $foo();
