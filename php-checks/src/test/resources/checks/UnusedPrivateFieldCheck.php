<?php

class C {
  private $field1;            // Noncompliant {{Remove this unused "$field1" private field.}}
//        ^^^^^^^
  private $field2;            // OK
  public $field3;             // OK
  private $myArray = [0, 1];  // OK
  private static $field4;     // OK
  private static $field5;     // OK

  public function f($field1) {
    return $field1 + $this->field2;
  }

  public function g() {
    return $this->myArray[0] + self::$field4 + static::$field5;
  }
}

class D {
  private $field1;  // OK
  private $field2;  // Noncompliant

  public function f($field2) {
    return $field2 + $this->field1;
  }

}

class E {
  private $field1;  // OK
  private $field2;  // OK

  public function f() {
    return "$this->field1 {$this->field2}";
  }

}

/**
 *  SONARPHP-402
 */
class HeredocUsage {
  private $a;

  function heredoc_usage() {
    echo <<<EOF
    {$this->a}
EOF;
  }
}
