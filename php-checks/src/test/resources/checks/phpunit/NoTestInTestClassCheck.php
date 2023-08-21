<?php

use PHPUnit\Framework\TestCase;
use PHPUnit\Framework\Attributes\Test;

class MyTest extends TestCase {} // Noncompliant {{Add some tests to this class.}}
//    ^^^^^^

class MyOtherTest extends TestCase { // OK
  public function testFoo() {}
}

class MyNextTest extends TestCase { // Noncompliant {{Add some tests to this class.}}
  public function foo() {}
}

class MyNextOtherTest extends TestCase { // OK
  /** @test */
  public function foo() {}
}

class MyLastTest extends TestCase { // Noncompliant {{Add some tests to this class.}}
//    ^^^^^^^^^^
  use TestTrait;
  /** @test */
  private function testFoo() {}
}

abstract class MyTest extends TestCase {} // OK

class MyAttribute0 extends TestCase { // Noncompliant {{Add some tests to this class.}}
//    ^^^^^^^^^^^^
  #[NotTestAttribute]
  public function foo() {}
}

class MyAttribute1 extends TestCase {
  #[Test]
  public function foo() {}
}

class MyAttribute2 extends TestCase { // Noncompliant {{Add some tests to this class.}}
//    ^^^^^^^^^^^^
  #[TestDox('It does something')]
  public function foo() {}
}

class MyAttribute3 extends TestCase {
  #[Test]
  #[DataProvider('additionProvider')]
  public function foo() {}
}

class MyAttribute4 extends TestCase { // Noncompliant
  #[\Foo\Test]
  public function foo() {}
}

class MyAttribute5 extends TestCase { // Noncompliant
  #[\Test]
  public function foo() {}
}

class MyAttribute6 extends TestCase { // Noncompliant
  #[Foo\Test]
  public function foo() {}
}

class MyAttribute7 extends TestCase {
  #[Test('foobar')]
  public function foo() {}
}

class MyAttribute8 extends TestCase {
  #[Test, TestDox('It does something')]
  public function foo() {}
}

class MyAttribute9 extends TestCase {
  #[TestDox('It does something'), Test]
  public function foo() {}
}

?>
