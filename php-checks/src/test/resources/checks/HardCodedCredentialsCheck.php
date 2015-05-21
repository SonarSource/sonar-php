<?php
class A {
    
  public $fieldNameWithPasswordInIt = retrievePassword();
  public $fieldNameWithPasswordInIt = ""; // Noncompliant
  public $fieldNameWithPasswordInIt = "$password";
  public $otherFieldName = "";
  
  private function a() {
    $variable1 = "blabla";
    $variable2 = "login=a&password=xxx"; // Noncompliant
    $variable3 = "login=a&password=";
    $variable4 = "login=a&password=$password";

    $variableNameWithPasswordInIt = "xxx"; // Noncompliant
    $otherVariableNameWithPasswordInIt;
    $this->fieldNameWithPasswordInIt = "xx"; // Noncompliant
    $this->fieldNameWithPasswordInIt = retrievePassword(); 
  }

}