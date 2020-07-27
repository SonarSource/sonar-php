<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase {
  /** @test */
  public function testA() {} // Noncompliant {{Add at least one assertion to this test case.}}
//                ^^^^^

   /** @test */
  public function testB() { // Compliant
    self::assertTrue(foo());
  }

  public function testC() { // Compliant
    $logger->expects($this->exactly(2))->method('debug');
  }

  public function testD() { // Compliant
    $this->doAssert();
    $this->doAssert();
  }

  private function doAssert() {
    self::assertTrue(foo());
  }

  public function testE() { // Noncompliant
    $x = "xyz";
    self::$x(foo());
  }

  /** @expectedException */
  public function testF() {
  }

  /** @doesNotPerformAssertions */
  public function testG() {
  }

  /** @expectedDeprecation */
  public function testH() {
  }

}
