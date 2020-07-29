<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
  public function test()
  {
    assertTrue($a === $b); // Noncompliant {{Use assertSame instead.}}
    assertTrue($a == $b); // Noncompliant {{Use assertEquals instead.}}
    assertTrue($a === null); // Noncompliant {{Use assertNull instead.}}
    assertTrue($a !== null); // Noncompliant {{Use assertNotNull instead.}}
    assertTrue($a !== $b); // Noncompliant {{Use assertNotSame instead.}}
    assertTrue($a != $b); // Noncompliant {{Use assertNotEquals instead.}}

    assertFalse($a === $b); // Noncompliant {{Use assertNotSame instead.}}
    assertFalse($a == $b); // Noncompliant {{Use assertNotEquals instead.}}
    assertFalse($a === null); // Noncompliant {{Use assertNotNull instead.}}
    assertFalse($a !== null); // Noncompliant {{Use assertNull instead.}}
    assertFalse($a === $b); // Noncompliant {{Use assertNotSame instead.}}
    assertFalse($a == $b); // Noncompliant {{Use assertNotEquals instead.}}

    assertTrue(!($a === null)); // Noncompliant {{Use assertNotNull instead.}}

    assertTrue(strpos($this->Mail->Body, 'src="composer.json"') === false); // OK
  }
}
