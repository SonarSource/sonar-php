<?php

use PHPUnit\Framework\TestCase;
use PHPUnit\Framework\Attributes\Test;

class ATest extends TestCase {
  public function testA() {
      $this->expectException(\RuntimeException::class);
      doSomething();
      $this->assertTrue($a); // Noncompliant {{Don't perform an assertion here; An exception is expected to be raised before its execution.}}
  }

  public function testB() {
      $this->expectException(\RuntimeException::class);
      $this->assertTrue(foo()); // Noncompliant {{Refactor this test; if this assertion's argument raises an exception, the assertion will never get executed.}}
  }

  /**
  * @expectedException \RuntimeException
  */
  public function testAnnotation() {
      foo();
      $this->assertTrue(true); // Noncompliant {{Don't perform an assertion here; An exception is expected to be raised before its execution.}}
  }

  public function testNoExpect() {
    $this->assertTrue(true); // Compliant
  }

  public function testNoAssertionAtEnd() {
    $this->expectException(\RuntimeException::class);
    $this->assertTrue(true);
    foo(); // Compliant
  }

  public function testExpectNotMainStatement() {
    if(itRains()) {
      $this->expectException(\SlipperyException::class);
    }
    $this->assertTrue(true); // Compliant
  }

  public function testInnerFunction() {
    $this->expectException(\RuntimeException::class);
    $foo = function() {
      $this->assertTrue(true); // Compliant
    };
  }

  /**
  * @expectedException \RuntimeException
  */
  public function testNoFunctionCall() {
    $x = "y";
  }

  public function testThrowAtEnd() {
    $this->expectException(\RuntimeException::class);
    try {
      doSomething();
      $this->fail();
    } catch(\FooException $e) {}

    $this->assertTrue($a); // Compliant
    throw $e;
  }
}

class BTest extends TestCase {
  #[Test]
  public function foo() {
      $this->expectException(\RuntimeException::class);
      doSomething();
      $this->assertTrue($a); // Noncompliant
  }
}

// For coverage
class Foo {
  public function a() {
  }
}

foo();
