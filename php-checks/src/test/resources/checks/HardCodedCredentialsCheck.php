<?php
class A {
    
  public $fieldNameWithPasswordInIt = retrievePassword();
  public $fieldNameWithPasswordInIt = ""; // NOK {{Remove this hard-coded password.}}
  public $fieldNameWithPasswordInIt = "$password";
  public $fieldNameWithPasswordInIt;
  public $otherFieldName = "";
  
  private function a() {
    $variable1 = "blabla";
    $variable2 = "login=a&password=xxx"; // NOK
    $variable3 = "login=a&password=";
    $variable4 = "login=a&password=$password";

    $variableNameWithPasswordInIt = "xxx"; // NOK
    $otherVariableNameWithPasswordInIt;
    $this->fieldNameWithPasswordInIt = "xx"; // NOK
    $this->fieldNameWithPasswordInIt = retrievePassword(); 
  }

}

const secretPassword = "xxx"; // NOK
const otherConstant = "xxx";

class A {
  const constFieldNameWithPasswordInIt = ""; // NOK
  const otherConstFieldName = "";
}

function foo() {
  static $staticVariableNameWithPasswordInIt = "xxx"; // NOK
  static $otherStaticVariableName = "xxx";
}
