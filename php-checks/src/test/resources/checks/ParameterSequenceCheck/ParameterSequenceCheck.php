<?php

//-----------------
// Function Calls
//-----------------
function testFunction($a, $b = null) { }

testFunction($a, $b); // Compliant
testFunction($b, $a); // Noncompliant
testFunction($a, $a); // Compliant

testFunction($c, $d); // Compliant
testFunction($a, $e); // Compliant
testFunction($f, $b); // Compliant
testFunction($a); // Compliant
otherFunction($b, $a); // Compliant
testFunction(a:$b, b:$a); // Compliant


//-----------------
// Self Method Calls
//-----------------
class BeforeClass {
  public function testMethod($b, $a) { }
}

class TestClass {
  public function __construct($a, $b) { }

  public static function testMethod($a, $b = null) { }

  public function otherTestMethod() {
    $this->testMethod($a, $b); // Compliant
    $this->testMethod($b, $a); // Noncompliant

    self::testMethod($a, $b); // Compliant
    self::testMethod($b, $a); // Noncompliant

    static::testMethod($a, $b); // Compliant
    static::testMethod($b, $a); // Noncompliant

    $a->testMethod($b, $a); // Compliant
  }
}

class AfterClass {
  public function testMethod($b, $a) { }
}


//-----------------
// Extended Method Calls
//-----------------
class ChildClass1 extends OtherClass {
  public static function otherTestMethod() {
    $this->testMethod($b, $a); // Compliant
  }
}

class ChildClass2 extends TestClass {
  public static function otherTestMethod() {
    $this->testMethod($b, $a); // Noncompliant
  }
}

//-----------------
// Object Creation
//-----------------
$testClass = new TestClass($a, $b); // Compliant
$testClass = new TestClass($b, $a); // False Negative

//-----------------
// Dynamic Method Calls
//-----------------
$testClass->testMethod($a, $b); // Compliant
$testClass->testMethod($f, $b); // Compliant
$testClass->testMethod($b, $a); // False Negative
$otherClass->testMethod($b, $a); // Compliant

// -----------------
// Static Method Calls
// -----------------
TestClass::testMethod($a, $b); // Compliant
TestClass::testMethod($f, $b); // Compliant
TestClass::testMethod($b, $a); // Noncompliant
OtherClass::testMethod($b, $a); // Compliant

// -----------------
// Anonymous Classes
// -----------------
$c = new class() {
   public function method1($a, $b) {}
   public function method2() {
    $this->method1($b, $a); // Noncompliant
   }
};

// -----------------
// Code Coverage
// -----------------
function functionWithoutParameters() {}
functionWithoutParameters($a);
functionWithoutParameters($a, $b);
$foo();
$foo::$bar($a, $b);

class CoverageClass {
  public function testMethod($a) {}

  public function coverageMethod() {
    $this->property->testMethod($a);
    $this->otherMethod($a)->testMethod($a);
    $this->$foo($a);
  }
}

class Date {} // Same symbol identifier as function symbol of 'date'
date($a, $b);
