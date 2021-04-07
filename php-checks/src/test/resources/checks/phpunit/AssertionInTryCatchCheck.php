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

    try {
      $this->assertEquals("foo", $bar); // Noncompliant
    } catch(Exception $e2) {
      echo "bar";
    }

    try {
      $this->assertEquals("foo", $bar); // Noncompliant
    } catch(ExpectationFailedException|SomeOtherException $e22) {
      echo "bar";
    }

    try {
      $this->assertEquals("foo", $bar); // Noncompliant
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    } catch(ExpectationFailedException|Exception $e22b) {
          //^^^^^^^^^^^^^^^^^^^^^^^^^^<^^^^^^^^^<
      echo "bar";
    }

    try {
      $this->assertEquals("foo", $bar); // Noncompliant
    } catch(SomeOtherException $e222) {
      echo $e222;
    } catch(ExpectationFailedException $e2222) {}

    try {
      $this->assertEquals("foo", $bar); // Compliant
    } catch(ExpectationFailedException $e3) {
      doSomethingWith($e3);
    }

    try {
      $this->notAssertion(); // Compliant
    } catch(ExpectationFailedException $e4) {
    }

    try {
        $this->assertEquals("foo", $bar); // Noncompliant {{Don't use this assertion inside a try-catch catching an assertion exception.}}
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    } catch(ExpectationFailedException) {
          //^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Exception type that catches assertion exceptions.}}
    }

    try {
        $this->assertEquals("foo", $bar); // Compliant
    } catch(SomeOtherException) {}
    
  }
}

// For coverage
class FooClass {
  public function fooFunction() {
    try {} catch(\Exception $e) {}
  }
}
