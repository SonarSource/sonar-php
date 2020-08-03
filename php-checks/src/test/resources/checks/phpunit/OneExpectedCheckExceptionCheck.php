<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
  public function testSomething()
  {
    try { // Noncompliant {{Refactor the body of this try/catch to have only one invocation throwing an exception.}}
//  ^^^
      g(y(1));
//    ^<^<
      $this->fail('RuntimeException is not thrown');
    } catch (RuntimeException $e) {}

    try { // Noncompliant
      g($y);
      $foo();
    } catch (RuntimeException $e) {}

    $y = y(1);
    try {
      g($y); // OK
      $this->fail('RuntimeException is not thrown by g()');
    } catch (RuntimeException $e) {}

    $y = y(1);
    try {
      $this->assertEquals('23',$y);
      g($y); // OK
      $this->fail('RuntimeException is not thrown by g()');
    } catch (RuntimeException $e) {}

    try {
        Coordinate::columnIndexFromString($cellAddress);
    } catch (\Exception $e) {
        self::assertInstanceOf(Exception::class, $e);
        return;
    }
    $this->fail('An expected exception has not been raised.');
  }
}

doSomething();
try{} catch (RuntimeException $e) {}
