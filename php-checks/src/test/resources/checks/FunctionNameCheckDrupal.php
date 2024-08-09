<?php
use Drupal\something;

function __construct() {  // OK
}

function __destruct() {   // OK
}

function do_something() {  // OK
}

function DoSomething() {  // Noncompliant {{Rename function "DoSomething" to match the regular expression ^[a-z][a-z0-9_]*$.}}
//       ^^^^^^^^^^^
}

class A {

  /**
   * @inheritdoc
   */
  public function DoSomething() {  // Noncompliant
  }

  function DoSomething() {  // Noncompliant {{Rename function "DoSomething" to match the regular expression ^[a-z][a-z0-9_]*$.}}
//         ^^^^^^^^^^^
  }

  /**
   * OK magic methods
   */
  public function __construct() {}
  public function __destruct() {}
  public function __call() {}
  public function __callStatic() {}
  public function __get() {}
  public function __set() {}
  public function __isset() {}
  public function __unset() {}
  public function __sleep() {}
  public function __wakeup() {}
  public function __toString() {}
  public function __invoke() {}
  public function __set_state() {}
  public function __clone() {}
  public function __debugInfo() {}

}

class A1 extends A {
  public function DoSomething() {} // OK, overrides A.DoSomething
  public function DoSomethingElse() {} // Noncompliant
}

class B extends Unknown {
  public function MayOverrideAnotherMethod() {}
}
