<?php

class A {

  private function __construct() {  // OK - private constructor
  }

  private function f() {            // Noncompliant {{Remove this unused private "f" method.}}
//                 ^
    $foo = clone $this;

    $code = '_i';
    echo "${foo->j()}";
    return $foo->g(1)
               ->h();
  }

  private function g($p1) {         // OK
  }

  private function h() {            // OK
  }

  private function _i() {           // OK - used in a simple string literal
  }

  private function j() {            // OK - used as encapsulated variable in string
  }

  public function k() {             // OK - public
  }

  private function __clone() {           // OK - magic method
  }

  function m() {                    // OK - default (=> not private)
  }

  static private function n() {     // Noncompliant
  }

}

class B {

  private $field;

  private function B() {              // OK - private constructor
  }


}


/**
 *  SONARPHP-402
 */
class HeredocUsage {
  private function foo() {}

  function heredoc_usage() {
    echo <<<EOF
    {$this->foo()}
EOF;
  }
}

$x = new class {

  private function __construct() {  // OK - private constructor
  }

  private function f() {            // Noncompliant {{Remove this unused private "f" method.}}
//                 ^
    $this->h();

  }

  private function h() {            // OK, used
  }

  public function k() {             // OK - public
  }

  function m() {                    // OK - default (=> not private)
  }

  static private function n() {     // Noncompliant
  }
};

class UsageInStringLiteral {
    private function Comparator($a, $b) {
        return $a - $b;
    }

    function sort($arr) {
        // function names are case insensitive
        usort($arr, ['self', 'COMPARATOR']);
    }
}

class ClassContainingAnonymousClass {

  /**
   * @uses five - mention a noncompliant method from another class to ensure it is not remembered from here
   */
  public function containsAnonymousClass($obj) {
    $anonymousClass = new class(){};
    $obj->foo();
    echo "${$obj->bar()}";
  }

  private function foo() {
  }

  private function bar() {
  }
}

// Enums
enum SimpleEnum {
  private function unused() {} // Noncompliant
  // For enums private and protected are equivalent as inheritance is not allowed.
  protected function unusedProtected() {} // Noncompliant

  public function publicFunction() {$x = "appearsInAString";}
  private function appearsInAString() {} // Compliant
}

trait SomeTrait {
  private function privateTraitFunction() {}
}

class DynamicClass
{
  /**
   * @uses one
   * @uses two()
   * @uses DynamicClass::three()
   * @uses DynamicClass::four
   */
  public function callDynamic(string $method): void
  {
    call_user_func([$this, $method]);
    // or
    $this->{$method}();
  }

  private function one(): void
  {
  }

  private function two(): void
  {
  }

  private function three(): void
  {
  }

  private function four(): void
  {
  }

  private function five(): void // Noncompliant
  {
  }
}

class UsageInFirstClassCallable
{
    public function getClockCallable(): callable {
        $callable = strtoupper(...);  // callables without `$this` shouldn't trigger the logic
        return $this->getTime(...);
    }

    private function getTime(): int {
        return time();
    }

    private function getDuration(): int {
        return time();
    }

    public function addQuery(Query $query): callable
    {
        $this->{'getDuration'}(...); // shouldn't trigger on callable convert with something other than name identifier
        return $query->getDuration(...); // shouldn't trigger on callable convert with receivers other that `$this`
    }
}

class MagicMethodCall
{
  public function __call($method, $arguments)
  {
    if (method_exists($this, $method)) {
      return call_user_func_array([$this, $method], $arguments);
    }
    trigger_error('Call to undefined method '.__CLASS__.'::'.$method.'()', E_USER_ERROR);
  }

  // OK because it might be called via __call()
  private function bar()
  {
  }
}

class MagicMethodCall2
{
  public function __call($method, $arguments)
  {
    return call_user_func($method, $arguments);
  }

  // OK because it might be called via __call()
  private function bar()
  {
  }
}

class MagicMethodCall3Base
{
  public function __call($method, $arguments)
  {
    return call_user_func($method, $arguments);
  }
}

class MagicMethodCall3Impl extends MagicMethodCall3Base
{
  // OK because it might be called via __call() in superclass
  private function bar()
  {
  }
}

class MagicMethodCall4Base
{
  public function __call($method, $arguments)
  {
    return call_user_func($method, $arguments);
  }
}

class MagicMethodCall4Abstract extends MagicMethodCall4Base {}

class MagicMethodCall4Impl extends MagicMethodCall4Abstract
{
  // OK because it might be called via __call() in superclass
  private function bar()
  {
  }
}

class MagicMethodCall5
{
  public function __call($method, $arguments)
  {
    // The call of call_user_func_array or call_user_func is missing
  }

  private function bar() // Noncompliant
  {
  }
}

class MagicMethodCall6Base {}

class MagicMethodCall6Abstract extends MagicMethodCall6Base {}

class MagicMethodCall6Impl extends MagicMethodCall6Abstract
{
  private function bar() // Noncompliant
  {
  }
}
