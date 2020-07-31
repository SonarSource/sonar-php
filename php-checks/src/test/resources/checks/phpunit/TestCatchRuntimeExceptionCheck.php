<?php

use PHPUnit\Framework\TestCase;

class MyRuntimeException extends RuntimeException {}

class MyTest extends TestCase
{
  /**
    * @expectedException RuntimeException
    */
  public function testTheThing() { // FN
    try {} catch (RuntimeException $e) {} // Noncompliant
//                ^^^^^^^^^^^^^^^^
    $this->expectException(RuntimeException::class); // Noncompliant
//                         ^^^^^^^^^^^^^^^^^^^^^^^
    $this->expectException(MyRuntimeException::class); // OK

    $this->expectException("RuntimeException"); // Noncompliant
//                         ^^^^^^^^^^^^^^^^^^

    $runtimeException = "RuntimeException";
    $this->expectException($runtimeException::class); // FN

    $this->expectException(get_class(new RuntimeException())); // FN - no one will do this

    $this->expectException(Exception::class); // OK
    $this->expectException(MyException::class); // OK
    $this->expectException("Exception"); // OK
    $this->expectException(MyException::$bar); // OK
    $this->expectException(); // OK
    $this->$foo();

    doSomething();
  }

  public function noTest() {
    try {} catch(Exception $e) {}
    doSomething();
  }
}
