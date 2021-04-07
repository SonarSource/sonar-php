<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
  public function test() {
    assertEquals($a, $a); // Noncompliant
    assertSame($a, $a); // Noncompliant
    assertNotEquals($a, $a); // Noncompliant
    assertNotSame($a, $a); // Noncompliant

    assertEquals($expected, $a); // OK
    assertSame($expected, $a); // OK
    assertNotEquals($expected, $a); // OK
    assertNotSame($expected, $a); // OK

    assertNotSame($expected);
    assertTrue($expected);

    $this->assertEquals('You must provide at least one recipient email address.', $this->Mail->ErrorInfo); //OK
  }
}
