<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase {
  public function testMethod() { // Noncompliant {{Identified as test method.}}
    $foo = $bar; // Noncompliant {{Identified assignment in the test class and method.}}
  }
}

class MyOtherTest extends PHPUnit\Framework\TestCase {
  public function testMethod() {} // Noncompliant {{Identified as test method.}}
}

class MyNextTest extends PHPUnit_Framework_TestCase {
  public function testMethod() {} // Noncompliant {{Identified as test method.}}
}

class MyNormalTest extends FooTest {
  public function testMethod() {} // OK
}

class MyLastTest extends TestCase {
  /** @test */
  public function method() {} // Noncompliant {{Identified as test method.}}
}

class MyRelyLastTest extends TestCase {
  public function method() {} // OK
}
