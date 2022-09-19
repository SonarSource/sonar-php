<?php

require_once __DIR__ . "/../vendor/autoload.php";

use PHPUnit\Framework\TestCase;
use SonarPhp\PhpUnit\Math;
use function PHPUnit\Framework\assertEquals;

/** @covers \SonarPhp\PhpUnit\Math */
class MathTest extends TestCase
{

  public function testAdd()
  {
    assertEquals(1 + 2, Math::add(1, 2));
    assertEquals(2 + 5, Math::add(2, 5));
  }

  public function testSub()
  {
    assertEquals(3 - 2, Math::sub(3, 2));
    assertEquals(0 - 4, Math::sub(0, 5));
  }

}
