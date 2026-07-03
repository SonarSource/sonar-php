<?php

ldap_bind("a", "b", ""); // Compliant

class A {

  public $fieldNameWithPasswordInIt = retrievePassword();
  public $fieldNameWithPasswordInIt = "azerty123"; // Noncompliant {{Detected 'password' in this variable name, review this potentially hardcoded credential.}}
//       ^^^^^^^^^^^^^^^^^^^^^^^^^^
  public $fieldNameWithPasswordInIt = ""; // OK, empty
  public $fieldNameWithPasswordInIt = ''; // OK, empty
  public $fieldNameWithPasswordInIt = "$password";
  public $fieldNameWithPasswordInIt;
  public $otherFieldName = "";

  // only a single issue even if multiple occurence of forbidden words
  public $myPasswordIsPWD = "something"; // Noncompliant {{Detected 'password' in this variable name, review this potentially hardcoded credential.}}
  public $myPasswordIsPWD = ""; // OK, empty

  private function a() {
    $variable1 = "blabla";
    $variable2 = "login=a&pwd=azerty123"; // Noncompliant {{Detected 'pwd' in this variable name, review this potentially hardcoded credential.}}
//               ^^^^^^^^^^^^^^^^^^^^^^^
    $variable3 = "login=a&password=";
    $variable4 = "login=a&password=$password";

    $variableNameWithPasswordInIt = "azerty123"; // Noncompliant
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    $otherVariableNameWithPasswordInIt;
    $this->fieldNameWithPasswdInIt = "azerty123"; // Noncompliant
//         ^^^^^^^^^^^^^^^^^^^^^^^
    $this->fieldNameWithPasswordInIt = retrievePassword();
  }

}

const secretPassword = "azerty123"; // Noncompliant
const otherConstant = "xxx";
const nonRelatedValue = 42;

class A {
  const constFieldNameWithPasswordInIt = "something"; // Noncompliant
  const constFieldNameWithPasswordInIt = ""; // OK, empty
  const otherConstFieldName = "";
}

function foo() {
  static $staticVariableNameWithPasswordInIt = "azerty123"; // Noncompliant
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
$var1 = "password=(secret)"; // Compliant, bracketed placeholder recognized by SecretClassifier
$var1 = "password=Tr8pQz2Ln9"; // Noncompliant

$pwd = "pwd"; // Compliant
$password = "pwd"; // Compliant, too short to be a real secret (SecretClassifier)
$password = "password"; // Compliant
$ampq_password = 'amqp-password'; // Compliant
const CONFIG_PATH_QUEUE_AMQP_PASSWORD = 'queue/amqp/password'; // Compliant
const IDENTITY_VERIFICATION_PASSWORD_FIELD = 'current_password'; // Compliant

// The literal string doesn't contain the wordlist item matched on the variable name
const DEFAULT_AMQP_PASSWORD = 'azerty123'; // Noncompliant

$uri = "ftp://user:azerty123@domain.com"; // Noncompliant {{Detected URI with password, review this potentially hardcoded credential.}}
$uri = "ssh://user:azerty123@domain.com"; // Noncompliant
$uri = "https://user:azerty123@domain.com"; // Noncompliant
$uri = "http://user:azerty123@domain.com"; // Noncompliant
$uri = "https://user:@domain.com"; // Compliant
$uri = "https://user@domain.com:80"; // Compliant
$uri = "http://user@domain.com"; // Compliant
$uri = "https://domain.com/user:azerty123"; // Compliant - valid url without credentials
$uri = "https://admin:admin@domain.com"; // Compliant
$uri = "scheme://:@localhost"; // Compliant
Request::create('http://user:password@test.com'); // Compliant - often used for tests
Request::create('https://username:password@test.com'); // Compliant - often used for tests
preg_match('#(^git://|\.git/?$|git(?:olite)?@|//git\.|//github.com/)#i', $url); // Compliant
$gitUri = "https://user:azerty@github.com/username/repository.git"; // Noncompliant

// Values recognized by SecretClassifier as known non-secrets don't raise, even though the name matches.
$password = "changeit"; // Compliant, well-known placeholder secret
$password = "Xk28"; // Compliant, too short to be a real secret
$password = '${SECRET_PASSWORD}'; // Compliant, variable interpolation
$password = "op://vault/secret"; // Compliant, external secret store reference
$password = "{cipher}1e3faa2cdab2deae117dca102e52922a"; // Compliant, encrypted marker
