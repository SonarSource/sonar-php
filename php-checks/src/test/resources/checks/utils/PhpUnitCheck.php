<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase {
  public function testMethod() { // Noncompliant {{Identified as test method.}}
    self::assertEquals(0, $code); // Noncompliant {{Identified as test assertion.}}
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

class MyAttribute1 extends TestCase {
  #[PHPUnit\Framework\Attributes\Test] // Noncompliant {{Identified as test method.}}
  public function foo() {}
}
