<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
  public function testSomething()
  {
    $this->markTestSkipped(); // Noncompliant
    $this->markTestIncomplete(); // Noncompliant
    markTestIncomplete(); // Noncompliant

    $this->markTestSkipped( 'The MySQLi extension is not available.' ); // Compliant
    $this->markTestIncomplete( 'Testing result validation is incomplete.' ); // Compliant

    $this->assertTrue($result->isValid());
  }
}

doSomething();
