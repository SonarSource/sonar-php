<?php

use PHPUnit\Framework\TestCase;
use PHPUnit\Framework\Attributes\Test;
use PHPUnit\Framework\Attributes\DoesNotPerformAssertions;

class MyTest extends TestCase {
  /** @test */
  public function testA() {} // Noncompliant {{Add at least one assertion to this test case.}}
//                ^^^^^

  public function testA2() { // Noncompliant
    doSomeStuff();
  }

   /** @test */
  public function testB() { // Compliant
    self::assertTrue(foo());
  }

  public function testC() { // Compliant
    $logger->expects($this->exactly(2))->method('debug');
  }

  public function testD() { // Compliant
    $this->doAssert();
    $this->doAssert();
  }

  private function doAssert() {
    self::assertTrue(foo());
  }

  public function testE() { // Compliant
    self::$x(foo());
  }

  public function testEE() { // Compliant
      $x(foo());
    }

  /** @expectedException */
  public function testF() { // Compliant
  }

  /** @doesNotPerformAssertions */
  public function testG() { // Compliant
  }

  #[DoesNotPerformAssertions]
  public function testG2() { // Compliant
  }

  #[DoesNotPerformAssertions]
  public function testBulkUpdateNonScalarParameterDDC1341(): void { // Compliant
    $this->_em->createQuery('UPDATE ' . CompanyEmployee::class)
              ->setParameter(0, new DateTime())
              ->setParameter(1, 'IT')
              ->execute();
  }

  /** @expectedDeprecation */
  public function testH() { // Compliant
  }

  public function testI() { // Compliant
    $this->addToAssertionCount(1);
  }

  public function testJ() { // Compliant
    $captcha = $this->createMock(DefaultModel::class);
    $captcha->method('isRequired')->willReturn($isRequired);
  }

  public function testK() { // Compliant
    doTest();
  }

  #[Test]
  public function testF() {} // Noncompliant

  /** @test */
  public function testG() {} // Noncompliant
}
