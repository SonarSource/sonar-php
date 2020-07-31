<?php

use PHPUnit\Framework\TestCase;
use PHPUnit\Framework\ExpectationFailedException;

class MyTest extends TestCase
{
  public function testFoo() {
    try {
      $this->assertEquals("foo", $bar); // Noncompliant {{Don't use this assertion inside a try-catch catching an assertion exception.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    } catch(ExpectationFailedException $e) {
          //^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Exception type that catches assertion exceptions.}}
    }
  }
}
