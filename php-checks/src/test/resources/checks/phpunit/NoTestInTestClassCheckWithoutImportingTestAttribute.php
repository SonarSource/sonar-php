<?php
use PHPUnit\Framework\TestCase;

class MyAttribute1 extends TestCase {
  #[PHPUnit\Framework\Attributes\Test]
  public function foo() {}
}

class MyAttribute2 extends TestCase { // Noncompliant
  #[Test]
  public function foo() {}
}
?>
