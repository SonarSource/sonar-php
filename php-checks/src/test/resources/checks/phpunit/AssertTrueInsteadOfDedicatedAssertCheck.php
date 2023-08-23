<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
  public function test()
  {
    assertTrue($a === $b); // Noncompliant {{Use assertSame() instead.}}
//  ^^^^^^^^^^^^^^^^^^^^^
    assertTrue($a == $b); // Noncompliant {{Use assertEquals() instead.}}
    assertTrue($a === null); // Noncompliant {{Use assertNull() instead.}}
    assertTrue($a !== null); // Noncompliant {{Use assertNotNull() instead.}}
    assertTrue($a !== $b); // Noncompliant {{Use assertNotSame() instead.}}
    assertTrue($a != $b); // Noncompliant {{Use assertNotEquals() instead.}}
    assertTrue($a == null); // Noncompliant {{Use assertNull() instead.}}
    assertTrue($a != null); // Noncompliant {{Use assertNotNull() instead.}}

    assertFalse($a === $b); // Noncompliant {{Use assertNotSame() instead.}}
    assertFalse($a == $b); // Noncompliant {{Use assertNotEquals() instead.}}
    assertFalse($a === null); // Noncompliant {{Use assertNotNull() instead.}}
    assertFalse($a !== null); // Noncompliant {{Use assertNull() instead.}}
    assertFalse($a === $b); // Noncompliant {{Use assertNotSame() instead.}}
    assertFalse($a == $b); // Noncompliant {{Use assertNotEquals() instead.}}
    assertFalse($a == null); // Noncompliant {{Use assertNotNull() instead.}}
    assertFalse($a != null); // Noncompliant {{Use assertNull() instead.}}

    assertTrue(!($a === null)); // Noncompliant {{Use assertNotNull() instead.}}

    assertTrue(null == null); // Noncompliant

    assertTrue(strpos($this->Mail->Body, 'src="composer.json"') === false); // Noncompliant {{Simplify this expression by using assertFalse() and removing the comparison to 'false'.}}
    assertTrue(strpos($this->Mail->Body, 'src="composer.json"') !== false); // Noncompliant {{Simplify this expression by removing the comparison to 'false'.}}

    assertTrue ($a == true);  // Noncompliant {{Simplify this expression by removing the comparison to 'true'.}}
    assertTrue ($a == false); // Noncompliant {{Simplify this expression by using assertFalse() and removing the comparison to 'false'.}}
    assertFalse($a == true);  // Noncompliant {{Simplify this expression by removing the comparison to 'true'.}}
    assertFalse($a == false); // Noncompliant {{Simplify this expression by using assertTrue() and removing the comparison to 'false'.}}
    assertTrue ($a != true);  // Noncompliant {{Simplify this expression by using assertFalse() and removing the comparison to 'true'.}}
    assertTrue ($a != false); // Noncompliant {{Simplify this expression by removing the comparison to 'false'.}}
    assertFalse($a != true);  // Noncompliant {{Simplify this expression by removing the comparison to 'true'.}}
    assertFalse($a != false); // Noncompliant {{Simplify this expression by using assertTrue() and removing the comparison to 'false'.}}
    assertTrue($a === true);  // Noncompliant
    assertTrue($a === false); // Noncompliant
    assertTrue(true == $a);  // Noncompliant
    assertTrue(false == $a); // Noncompliant
    assertTrue ($a == TRue);  // Noncompliant {{Simplify this expression by removing the comparison to 'true'.}}

    assertEquals($a, $b);
    doSomeThing();
  }

  public function not_a_test()
  {
    doSomeThing();
  }
}
