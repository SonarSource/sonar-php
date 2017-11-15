<?php
class A {

  public $fieldNameWithPasswordInIt = retrievePassword();
  public $fieldNameWithPasswordInIt = ""; // Noncompliant {{'password' detected in this variable name, review this potentially hardcoded credential.}}
//       ^^^^^^^^^^^^^^^^^^^^^^^^^^
  public $fieldNameWithPasswordInIt = "$password";
  public $fieldNameWithPasswordInIt;
  public $otherFieldName = "";

  // only a single issue even if multiple occurence of forbidden words
  public $myPasswordIsPWD = ""; // Noncompliant {{'password' detected in this variable name, review this potentially hardcoded credential.}}

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
  const constFieldNameWithPasswordInIt = ""; // Noncompliant
  const otherConstFieldName = "";
}

function foo() {
  static $staticVariableNameWithPasswordInIt = "xxx"; // Noncompliant
  static $otherStaticVariableName = "xxx";
}
