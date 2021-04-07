<?php
class A {

  public $pwd = "";
  public $password = "";
  public $passwd = "";

  public $iReallyLikeLadyMarmalade = "haha"; // Noncompliant {{'marmalade' detected in this variable name, review this potentially hardcoded credential.}}
//       ^^^^^^^^^^^^^^^^^^^^^^^^^
  public $someMarmalade = ""; // OK, empty

  private function foo() {
    $variable1 = "blabla";
    $variable2 = "login=a&pwd=xxx"; // Compliant
    $worms = "bazooka=xxx"; // Noncompliant {{'bazooka' detected in this variable name, review this potentially hardcoded credential.}}
//           ^^^^^^^^^^^^^
  }
}
