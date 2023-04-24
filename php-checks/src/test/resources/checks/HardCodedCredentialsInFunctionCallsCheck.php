<?php

use Abraham\TwitterOAuth\TwitterOAuth;

class ConstructorTestService
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

class ConstructorTestService
{
    private string $shouldBePassing;

    public function setPassword(string $shouldBePassing): void
    {
        $this->shouldBePassing = $shouldBePassing;
    }

    public function createKey(): KeyOrPassword
    {
        $password = "example";
        new TwitterOAuth("shouldBePassing", $shouldBePassing, $shouldBePassing);
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

$keyOrPwObj = new KeyOrPassword();
$keyOrPwObj->createFromPassword($key); // FN, parser is not able to resolve fqn of object

$otherKeyOrPwObj = 'Defuse\Crypto\KeyOrPassword';
$otherKeyOrPwObj::createFromPassword($key); // FN, parser not able to resolve fqn

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



