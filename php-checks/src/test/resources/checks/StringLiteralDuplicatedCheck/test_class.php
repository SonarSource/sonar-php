<?php

use PHPUnit\Framework\TestCase;

class UserServiceTest extends TestCase
{
    public function testCreateUser(): void
    {
        $user = new User('john@example.com');
        $this->assertEquals('john@example.com', $user->getEmail());
    }

    public function testDuplicateUser(): void
    {
        $existing = new User('john@example.com');
        $this->expectException(DuplicateUserException::class);
        new User('john@example.com');
    }

    public function testAnotherMethod(): void
    {
        $this->assertSame('john@example.com', $this->getEmail());
    }
}

// Strings outside test class still raise issues
echo "some value here"; // Noncompliant {{Define a constant instead of duplicating this literal "some value here" 3 times.}}
//   ^^^^^^^^^^^^^^^^^
echo "some value here";
//   ^^^^^^^^^^^^^^^^^< {{Duplication.}}
echo "some value here";
//   ^^^^^^^^^^^^^^^^^< {{Duplication.}}
