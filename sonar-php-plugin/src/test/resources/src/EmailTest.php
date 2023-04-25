<?php

use PHPUnit\Framework\TestCase;

final class EmailTest extends TestCase
{
  public function testCanBeCreatedFromValidEmail(): void
  {
    $string = 'user@example.com';

    $email = Email::fromString($string);

    $this->assertSame($string, $email->asString());
  }

  public function testCannotBeCreatedFromInvalidEmail(): void
  {
    $this->expectException(InvalidArgumentException::class);

    Email::fromString('invalid');
  }
}

