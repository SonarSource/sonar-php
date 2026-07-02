<?php
class A {

  public $pwd = "";
  public $password = "";
  public $passwd = "";

  public $iReallyLikeLadyMarmalade = "azerty123"; // Noncompliant {{Detected 'marmalade' in this variable name, review this potentially hardcoded credential.}}
//       ^^^^^^^^^^^^^^^^^^^^^^^^^
  public $someMarmalade = ""; // OK, empty

  private function foo() {
    $variable1 = "blabla";
    $variable2 = "login=a&pwd=xxx"; // Compliant
    $worms = "bazooka=azerty123"; // Noncompliant {{Detected 'bazooka' in this variable name, review this potentially hardcoded credential.}}
//           ^^^^^^^^^^^^^^^^^^^
  }
}
