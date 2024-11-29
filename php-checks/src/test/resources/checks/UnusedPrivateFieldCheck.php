<?php

class C {
  private $field1;            // Noncompliant {{Remove this unused "$field1" private field.}}
//        ^^^^^^^
  private $field2;            // OK
  public $field3;             // OK
  private $myArray = [0, 1];  // OK
  private static $field4;     // OK
  private static $field5;     // OK

  public function __construct(
    public $promotedPublic,
    private $promotedPrivateUsed,
    private $promotedPrivateUnused // Noncompliant
  ) {}

  public function f($field1) {
    return $field1 + $this->field2 + $this->promotedPrivateUsed;
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

/**
 * https://jira.sonarsource.com/browse/SONARPHP-1049
 */
class ConstantUsage {

  public function __construct()
  {
    $anonymousClass = new class {
      private const ANONYMOUS_UNUSED_PRIVATE_CONST = "foo"; // FN - would made the check to complex
      public const PUBLIC_CONST = self::UNUSED_PRIVATE_CONST;
    };
  }

  private const UNUSED_PRIVATE_CONST = 'foo'; // Noncompliant
  private const PRIVATE_CONST = 'foo';
  public const PUBLIC_CONST = self::PRIVATE_CONST;

  public const OTHER_PUBLIC_CONST = self::OTHER_PRIVATE_CONST;
  private const OTHER_PRIVATE_CONST = 'foo';

  public const ANOTHER_PUBLIC_CONST = ConstantUsage::ANOTHER_PRIVATE_CONST;
  private const ANOTHER_PRIVATE_CONST = 'foo';

  public const YET_ANOTHER_PUBLIC_CONST = OtherClass::OTHER_CONST;
  public const LAST_PUBLIC_CONST = \OtherNamespace\ConstantUsage::OTHER_CONST;
  private const OTHER_CONST = 'foo'; // Noncompliant

  public const PURPOSES = [self::PURPOSE_FUNCTIONAL, self::PURPOSE_ANALYTICAL];
  private const PURPOSE_ANALYTICAL = 'analytical';
  private const PURPOSE_FUNCTIONAL = 'functional';
}

//code coverage
class CodeCoverageClass {
  public function codeCoverageMethod() {
    echo self::$bar;
    echo $foo::BAR;
  }
}

class B {
  public const A = self::B;
  private const B = "foo";
}

class A {
  private const B = "bar"; //Noncompliant
}

class CaseSensitiveClassMembers {
  private $a = "foo"; //Noncompliant
  private $A = "Foo";

  private const b = "bar"; //Noncompliant
  private const B = "bar";

  public function action() {
    echo $this->A;
    echo self::B;
  }
}

