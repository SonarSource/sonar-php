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

class TestClass extends PHPUnit\Framework\TestCase
{
    public function createKey(): KeyOrPassword
    {
        $password = "example";
        new TwitterOAuth("shouldBePassing", $shouldBePassing, $password); // Compliant, as inside test class
    }
}

use PHPUnit\Framework\TestCase;

class TestClass extends TestCase
{
    public function createKey(): KeyOrPassword
    {
        $password = "example";
        new TwitterOAuth("shouldBePassing", $shouldBePassing, $password); // Compliant, as inside test class
    }
}

class NoClass
{
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


use PHPUnit\Framework\AssertionFailedError;
$error;
$this->error = new $AssertionFailedError('Only tests with the `@group legacy` annotation can have `@expectedDeprecation`.');

new Foo;

ldap_bind("a", "b", "p4ssw0rd"); // Noncompliant {{Revoke and change this password, as it is compromised.}}
//                  ^^^^^^^^^^
$test_connection_1 = new PDO("a", "b", "p4ssw0rd"); // Noncompliant
mysqli_connect("a", "b", "p4ssw0rd"); // Noncompliant
mysql_connect("a", "b", "p4ssw0rd"); // Noncompliant
oci_connect("a", "p4ssw0rd"); // Noncompliant
ldap_exop_passwd("a", "b", "c", "p4ssw0rd"); // Noncompliant
mssql_connect("a", "b", "p4ssw0rd"); // Noncompliant
odbc_connect("a", "b", "p4ssw0rd"); // Noncompliant
db2_connect("a", "b", "p4ssw0rd"); // Noncompliant
cubrid_connect("a", "b", "c", "d", "p4ssw0rd"); // Noncompliant
maxdb_connect("a", "b", "p4ssw0rd"); // Noncompliant
$test_connection_2 = new maxdb("a", "b", "p4ssw0rd"); // Noncompliant
maxdb_change_user("a", "b", "p4ssw0rd"); // Noncompliant
imap_open("a", "b", "p4ssw0rd"); // Noncompliant
ifx_connect("a", "b", "p4ssw0rd"); // Noncompliant
dbx_connect("a", "b", "c", "d", "p4ssw0rd"); // Noncompliant
fbsql_pconnect("a", "b", "p4ssw0rd"); // Noncompliant
$test_connection_3 = new mysqli("a", "b", "p4ssw0rd"); // Noncompliant

ldap_bind("a", "b"); // Compliant
ldap_bind("a", "b", $foo); // Compliant
ldap_bind("a", "b", ""); // Compliant

ldap_bind("a", "b", 2, 3, $par, password: "p4ssw0rd"); // Noncompliant
ldap_bind("a", "b", 2, 3, $par, passwd: "p4ssw0rd"); // Compliant
mysqli_connect("a", "b", password: "p4ssw0rd"); // Noncompliant
$test_connection_4 = new PDO(password: "p4ssw0rd"); // Noncompliant

namespace laravel;
use Illuminate\Encryption\Encrypter;
$enc = new Encrypter('staticKey'); // Noncompliant

namespace notlaravel;
use my\Other\Encrypter;
$enc = new Encrypter('staticKey'); // Compliant
