<?php
use PHPUnit\Framework\TestCase;

class MyTest extends TestCase {
  public function testA() {
    try {
      doSomething();
      $this->fail();
    } catch (Exception $e) { // Noncompliant {{Use expectException() to verify the exception throw.}}
    //                 ^^
      $this->assertEquals("abc", $e->getMessage());
    //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Use expectExceptionMessage() instead.}}
      $this->assertEquals(123, $e->getCode());
    //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Use expectExceptionCode() instead.}}
    }
  }

  public function testA1() {
     try {
      doSomething();
      $this->fail();
     } catch(Exception $e) { // Noncompliant
     }
  }

  public function testA11() {
     try {
      doSomething();
      $this->fail();
     } catch(Exception $e) { // Noncompliant
      $this->assertSame("message", $e->getMessage());
     }
  }

  public function testA111() {
     try {
      doSomething();
      $this->fail();
     } catch(Exception $e) { // Noncompliant
      $this->assertEquals(123, $e->getCode());
     }
  }

  public function testA1111() {
       try {
        doSomething();
        $this->fail();
       } catch(Exception $e) { // Compliant - Switch is not last statement in method.
        $this->assertEquals(123, $e->getCode());
       }
       assertTrue(doSomething());
    }

  public function testA2() {
     try {
      doSomething();
     } catch(Exception $e) {} // Compliant - does not contain call to fail().
  }

  public function testA3() {
     try {
      doSomething();
      $this->fail();
     } catch(Exception $e) { // Compliant - the catch block contains other things than just checking the message and/or code
      doSomethingElse();
     }
  }

  public function testA33() {
     try {
      doSomething();
      $this->fail();
     } catch(Exception $e) { // Compliant - the catch block contains other things than just checking the message and/or code
      $this->assertEquals("abc", getValue()->foo());
      $this->assertEquals("abc", $foo->foo());
     }
  }

  public function testA4() {
     try {
      doSomething();
      $this->fail();
     } catch(Exception $e) { // Compliant - the catch block contains other things than just checking the message and/or code
      $this->assertTrue(foo());
     }
  }
}

// For coverage
class FooClass extends TestCase {
  public function testFoo() {
    try {
    } catch (Exception $e) {
    } catch (OtherException $f) {
    }
  }

  public function testBar() {
    try {
      doSomething();
      if ($x) {
        $this->fail();
      }
    } catch (Exception $e) { // Compliant
    }
  }

  public function testBar2() {
    try {
      doSomething();
      $x = 1;
    } catch(Exception $e) {}
  }

  public function noTest() {
    try {
    } catch(Exception $e) {}
  }
}
