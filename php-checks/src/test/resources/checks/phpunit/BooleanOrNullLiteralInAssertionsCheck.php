<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase {
  function testFoo() {
    assertEquals(true, $x); // Noncompliant {{Use assertTrue() instead.}}
    assertEquals(false, $x); // Noncompliant {{Use assertFalse() instead.}}
    assertEquals(null, $x); // Noncompliant {{Use assertNull() instead.}}

    assertNotEquals(true, $x); // Noncompliant {{Use assertFalse() instead.}}
    assertNotEquals(false, $x); // Noncompliant {{Use assertTrue() instead.}}
    assertNotEquals(null, $x); // Noncompliant {{Use assertNotNull() instead.}}

    assertSame(true, $x); // Noncompliant {{Use assertTrue() instead.}}
    assertSame(false, $x); // Noncompliant {{Use assertFalse() instead.}}
    assertSame(null, $x); // Noncompliant {{Use assertNull() instead.}}

    assertNotSame(true, $x); // Noncompliant {{Use assertFalse() instead.}}
    assertNotSame(false, $x); // Noncompliant {{Use assertTrue() instead.}}
    assertNotSame(null, $x); // Noncompliant {{Use assertNotNull() instead.}}

    assertEquals($x, true); // Noncompliant {{Use assertTrue() instead.}}

    assertSame($x, $y); // Compliant
    assertContains(true, [true,false]); // Compliant
  }
}

// For coverage
class Foo {
  public function bar () {
    foo();
  }
}
