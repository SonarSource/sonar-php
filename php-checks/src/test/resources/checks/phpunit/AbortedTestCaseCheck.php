<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
  public function testSomething()
  {
    $this->markTestSkipped(); // Noncompliant {{Either remove this call or add an explanation about why the test is aborted.}}
    $this->markTestIncomplete(); // Noncompliant
    markTestIncomplete(); // Noncompliant

    $this->markTestSkipped( 'The MySQLi extension is not available.' ); // Compliant
    $this->markTestIncomplete( 'Testing result validation is incomplete.' ); // Compliant

    $this->assertTrue($result->isValid());
  }
}

doSomething();
