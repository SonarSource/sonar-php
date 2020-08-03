<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
  public function someFunction() { // Noncompliant {{Mark this method as test so that it can be executed by the test runner.}}
    $this->assertTrue("abc");
  }

  public function testA() {
    $this->someOtherFunction();
  }
  public function someOtherFunction() { // Compliant
    $this->assertTrue("abc");
  }
}
