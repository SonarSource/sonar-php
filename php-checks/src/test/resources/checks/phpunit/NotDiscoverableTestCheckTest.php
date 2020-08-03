<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
  public function someFunction() { // Noncompliant {{Mark this method as test so that it can be executed by the test runner.}}
    $this->assertTrue("abc");
  }

  private function testA() { // Noncompliant {{Adjust the visibility of this test method so that it can be executed by the test runner.}}
    $this->assertTrue("abc");
  }
  // @test
  private function b() { // Noncompliant {{Adjust the visibility of this test method so that it can be executed by the test runner.}}
      $this->assertTrue("abc");
    }
  private function a() { // Compliant - not marked as test
    $this->assertTrue("abc");
  }

  public function setUp() { // Compliant
    $this->assertTrue("abc");
  }

  public function testB() {
    $this->someOtherFunction();
  }
  public function someOtherFunction() { // Compliant
    $this->assertTrue("abc");
  }
}

abstract class FooClass extends TestCase {
  public function foo() { // Compliant
    $this->assertTrue("abc");
  }
}

// For coverage
class BarClass {
  public function bar() { // Compliant
    $this->assertTrue("abc");
    foo();
  }
}

class BarTest extends TestCase {
  public function testBar() {
    foo();
    $bar->xbar();
  }

  public function bar() {
  }

  public function foo() {
    foo();
    new AssertNotNull();
  }
}
