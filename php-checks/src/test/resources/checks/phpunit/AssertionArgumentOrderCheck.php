<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
  public function test()
  {
    self::assertEquals($runner.getExitCode(), 0); // Noncompliant {{Swap these 2 arguments so they are in the correct order: expected value, actual value.}}
    //                 ^^^^^^^^^^^^^^^^^^^^^> ^ {{Other argument to swap.}}
    self::assertEquals($result->format('d-M-Y'), '31-Jan-2012'); // Noncompliant
    self::assertEquals(0, $runner.getExitCode()); // OK
    self::assertEquals(0, 1); // OK
    $expected = 12;
    self::assertEquals($runner.getExitCode(), $expected); // Noncompliant
    assertEquals($runner.getExitCode(), $expected); // Noncompliant
    //           ^^^^^^^^^^^^^^^^^^^^^> ^^^^^^^^^
    self::assertEquals($bar, $foo); // OK
    self::assertTrue(12); // OK
    self::assertFileExists('test/testbootstrap.php', 'Message'); // OK - No expected value
    $this->assertStringEqualsFile($targetFilePath, 'SOURCE FILE'); // Noncompliant

    self::assertEquals(actual: $runner.getExitCode(), expected: 0);

    self::assertEquals($foo); // OK - No valid assertion of moodle test cases
    doSomethind($result->format('d-M-Y'), '31-Jan-2012'); // OK - No assertion

    $e = null;
    $this->assertInstanceOf(LogicException::class, $e); // OK - expected is a class name
    // Noncompliant@+1
    $this->assertInstanceOf(LogicException::CONSTANT, $e); // actual is a null literal, can't say anything about expected
    $this->assertInstanceOf($e, LogicException::class);
    $this->assertInstanceOf(\LogicException::class, $e);
    $this->assertNotInstanceOf(LogicException::class, $e);

    self::assertSame(0, $runner.getExitCode()); // OK
    $this->assertSame($expected, $runner.getExitCode()); // OK
    $this->assertSame($runner.getExitCode(), $expected); // Noncompliant
    $this->assertNotSame($expected, $runner.getExitCode()); // OK
    $this->assertNotSame($runner.getExitCode(), $expected); // Noncompliant
  }

  private function no_test() {
    doSomethind();
  }

  public function testExport(bool $foobar, bool $staticValueExpected = false)
  {
    $isStaticValue = true;
    $this->assertSame($staticValueExpected, $isStaticValue); // OK
    $this->assertSame(true, $isStaticValue); // OK
  }
}
