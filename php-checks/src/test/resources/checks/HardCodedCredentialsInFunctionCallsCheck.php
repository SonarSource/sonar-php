<?php

use Abraham\TwitterOAuth\TwitterOAuth;
$key2 = "example";
new TwitterOAuth("test1", $key2); // Noncompliant


use Defuse\Crypto\KeyOrPassword;

$key = "example";
KeyOrPassword::createFromPassword($key); // Noncompliant
KeyOrPassword::createFromPassword(password: $key); // Noncompliant
$password = "example";
KeyOrPassword::createFromPassword(password: $password); // Noncompliant
KeyOrPassword::createFromPassword($password); // Noncompliant


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


class PasswordService
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




