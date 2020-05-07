<?php

class A {

  public $fieldNameWithPasswordInIt = retrievePassword();
  public $fieldNameWithPasswordInIt = "xxx"; // Noncompliant {{'password' detected in this variable name, review this potentially hardcoded credential.}}
//       ^^^^^^^^^^^^^^^^^^^^^^^^^^
  public $fieldNameWithPasswordInIt = ""; // OK, empty
  public $fieldNameWithPasswordInIt = ''; // OK, empty
  public $fieldNameWithPasswordInIt = "$password";
  public $fieldNameWithPasswordInIt;
  public $otherFieldName = "";

  // only a single issue even if multiple occurence of forbidden words
  public $myPasswordIsPWD = "something"; // Noncompliant {{'password' detected in this variable name, review this potentially hardcoded credential.}}
  public $myPasswordIsPWD = ""; // OK, empty

  private function a() {
    $variable1 = "blabla";
    $variable2 = "login=a&pwd=xxx"; // Noncompliant {{'pwd' detected in this variable name, review this potentially hardcoded credential.}}
//               ^^^^^^^^^^^^^^^^^
    $variable3 = "login=a&password=";
    $variable4 = "login=a&password=$password";

    $variableNameWithPasswordInIt = "xxx"; // Noncompliant
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    $otherVariableNameWithPasswordInIt;
    $this->fieldNameWithPasswdInIt = "xx"; // Noncompliant
//         ^^^^^^^^^^^^^^^^^^^^^^^
    $this->fieldNameWithPasswordInIt = retrievePassword();
  }

}

const secretPassword = "xxx"; // Noncompliant
const otherConstant = "xxx";
const nonRelatedValue = 42;

class A {
  const constFieldNameWithPasswordInIt = "something"; // Noncompliant
  const constFieldNameWithPasswordInIt = ""; // OK, empty
  const otherConstFieldName = "";
}

function foo() {
  static $staticVariableNameWithPasswordInIt = "xxx"; // Noncompliant
  static $otherStaticVariableName = "xxx";
}

$var1 = "password=?"; // Compliant
$var1 = "password=:password"; // Compliant
$var1 = "password=:param"; // Compliant
$var1 = "password=%s"; // Compliant
$var1 = "password='" . pwd . "'"; // Compliant
$var1 = "password=" . pwd . "'"; // Compliant
$var1 = "password=?&login=a"; // Compliant
$var1 = "password=:password&login=a"; // Compliant
$var1 = "password=:param&login=a"; // Compliant
$var1 = "password=%s&login=a"; // Compliant
$var1 = "password=(secret)"; // Noncompliant

$pwd = "pwd"; // Compliant
$password = "pwd"; // Noncompliant
$password = "password"; // Compliant
$ampq_password = 'amqp-password'; // Compliant
const CONFIG_PATH_QUEUE_AMQP_PASSWORD = 'queue/amqp/password'; // Compliant
const IDENTITY_VERIFICATION_PASSWORD_FIELD = 'current_password'; // Compliant

// The literal string doesn't contain the wordlist item matched on the variable name
const DEFAULT_AMQP_PASSWORD = 'pwd'; // Noncompliant

$uri = "scheme://user:azerty123@domain.com"; // Noncompliant
$uri = "ssh://user:azerty123@domain.com"; // Noncompliant
$uri = "scheme://user:@domain.com"; // Compliant
$uri = "scheme://user@domain.com:80"; // Compliant
$uri = "scheme://user@domain.com"; // Compliant
$uri = "scheme://domain.com/user:azerty123"; // Compliant - valid url without credentials
$uri = "scheme://admin:admin@domain.com"; // Compliant
Request::create('http://user:password@test.com'); // Compliant - often used for tests
Request::create('https://username:password@test.com'); // Compliant - often used for tests
