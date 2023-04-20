<?php

use Abraham\TwitterOAuth\TwitterOAuth;

class PasswordService
{
    private string $shouldBePassing;

    public function setPassword(string $shouldBePassing): void
    {
        $this->shouldBePassing = $shouldBePassing;
    }

    public function createKey(): KeyOrPassword
    {
        $password = "example";
        new TwitterOAuth("shouldBePassing", $shouldBePassing, $password); // Noncompliant
    }
}


use Defuse\Crypto\KeyOrPassword;

$key = "example";
KeyOrPassword::createFromPassword($key); // Noncompliant
KeyOrPassword::createFromPassword(password: $key); // Noncompliant
$password = "example";
KeyOrPassword::createFromPassword(password: $password); // Noncompliant
KeyOrPassword::createFromPassword($password); // Noncompliant

$duplicateAssignment ="example";
$duplicateAssignment ="example";
KeyOrPassword::createFromPassword($duplicateAssignment); // FN, parser not able to resolve multiple assignments to one variable

KeyOrPassword::notASensitiveMethod($password);


class PasswordService
{
    private string $password;

    public function setPassword(string $password): void
    {
        $this->password = $password;
    }

    public function createKey(): KeyOrPassword
    {
        return KeyOrPassword::createFromPassword($password);
    }
}


class PasswordServiceSecond
{
    private string $password;

    public function setPassword(string $password): void
    {
        $this->password = $password;
    }

    public function createKey(): KeyOrPassword
    {
        $password = "example";
        return KeyOrPassword::createFromPassword($password); // Noncompliant
    }
}




