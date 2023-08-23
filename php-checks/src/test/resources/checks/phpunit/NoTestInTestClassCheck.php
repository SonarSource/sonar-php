<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase {} // Noncompliant {{Add some tests to this class.}}
//    ^^^^^^

class MyOtherTest extends TestCase { // OK
  public function testFoo() {}
}

class InheritingTest extends MyOtherTest {} // OK

class DoubleInheritingTest extends InheritingTest {} // OK

class MyNextTest extends TestCase { // Noncompliant {{Add some tests to this class.}}
  public function foo() {}
}

class MyNextOtherTest extends TestCase { // OK
  /** @test */
  public function foo() {}
}

class MyLastTest extends TestCase { // Noncompliant {{Add some tests to this class.}}
//    ^^^^^^^^^^
  use TestTrait;
  /** @test */
  private function testFoo() {}
}

abstract class MyTest extends TestCase {} // OK
