<?php

require_once __DIR__ . "/../vendor/autoload.php";

use PHPUnit\Framework\TestCase;
use SonarPhp\PhpUnit\Math;
use function PHPUnit\Framework\assertEquals;

/** @covers \SonarPhp\PhpUnit\Math */
class MathDivTest extends TestCase
{

  public function testDiv()
  {
    assertEquals(1 / 2, Math::div(1, 2));
    assertEquals(5 / 2, Math::div(5, 3));
  }

}
