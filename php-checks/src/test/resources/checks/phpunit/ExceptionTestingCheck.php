<?php
use PHPUnit\Framework\TestCase;

class MyTest extends TestCase {
  public function testA() {
    try {
      doSomething();
      $this->fail();
    } catch (Exception $e) { // Noncompliant
      $this->assertEquals("abc", $e->getMessage());
    }
  }
}
