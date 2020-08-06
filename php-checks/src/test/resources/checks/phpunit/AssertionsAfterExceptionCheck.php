<?php

use PHPUnit\Framework\TestCase;

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
}


// For coverage
class Foo {
  public function a() {
  }
}

foo();
