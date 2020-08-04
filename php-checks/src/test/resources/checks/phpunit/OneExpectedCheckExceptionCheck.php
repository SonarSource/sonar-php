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
        Coordinate::columnIndexFromString($cellAddress); // OK
    } catch (\Exception $e) {
        self::assertInstanceOf(Exception::class, $e);
        return;
    }
    $this->fail('An expected exception has not been raised.');

    try {
     g($y); // OK
     $this->fail(sprintf('RuntimeException is not thrown by %s',$functionName));
    } catch (RuntimeException $e) {}

    try { // Noncompliant
//  ^^^
     $foo::chain1('foo')->chain2('bar')->chain3('foobar')->chain4();
//         ^^^^^^<        ^^^^^^<        ^^^^^^<           ^^^^^^<
    } catch (RuntimeException $e) {}
  }
}

doSomething();
try{} catch (RuntimeException $e) {}
