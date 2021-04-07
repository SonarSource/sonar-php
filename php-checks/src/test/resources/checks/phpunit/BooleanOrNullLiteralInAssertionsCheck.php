<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase {
  function testFoo() {
    assertEquals(true, $x); // Noncompliant {{Use assertTrue() instead.}}
    assertEquals(false, $x); // Noncompliant {{Use assertFalse() instead.}}
    assertEquals(null, $x); // Noncompliant {{Use assertNull() instead.}}
    assertEquals('str', $x);

    assertNotEquals(true, $x); // Noncompliant {{Use assertNotTrue() instead.}}
    assertNotEquals(false, $x); // Noncompliant {{Use assertNotFalse() instead.}}
    assertNotEquals(null, $x); // Noncompliant {{Use assertNotNull() instead.}}

    assertSame(true, $x); // Noncompliant {{Use assertTrue() instead.}}
    assertSame(false, $x); // Noncompliant {{Use assertFalse() instead.}}
    assertSame(null, $x); // Noncompliant {{Use assertNull() instead.}}

    assertNotSame(true, $x); // Noncompliant {{Use assertNotTrue() instead.}}
    assertNotSame(false, $x); // Noncompliant {{Use assertNotFalse() instead.}}
    assertNotSame(null, $x); // Noncompliant {{Use assertNotNull() instead.}}

    assertEquals($x, true); // Noncompliant {{Use assertTrue() instead.}}

    assertEquals();
    assertEquals(true);

    assertTrue($x); // Compliant
    assertFalse($x); // Compliant
    assertNull($x); // Compliant
    assertNotNull($x); // Compliant

    assertSame($x, $y); // Compliant
    assertContains(true, [true,false]); // Compliant
  }

  function namedArguments() {
    assertEquals(message: '', actual: $x, expected: 0);
    assertEquals(message: null, actual: $x, expected: 0);
    assertEquals(message: '', actual: $x, expected: null); // Noncompliant {{Use assertNull() instead.}}
  }
}

// For coverage
class Foo {
  public function bar() {
    foo();
  }
}

class Bar extends TestCase {
  public function testBar() {
    $this->assertEquals($x);
  }
}
