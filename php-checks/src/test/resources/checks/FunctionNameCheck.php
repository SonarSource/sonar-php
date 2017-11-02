<?php

function __construct() {  // OK
}

function __destruct() {   // OK
}

function doSomething() {  // OK
}

function DoSomething() {  // Noncompliant {{Rename function "DoSomething" to match the regular expression ^[a-z][a-zA-Z0-9]*$.}}
//       ^^^^^^^^^^^
}

class A {

  /**
   * @inheritdoc
   */
  public function DoSomething() {  // OK
  }

  function DoSomething() {  // Noncompliant {{Rename function "DoSomething" to match the regular expression ^[a-z][a-zA-Z0-9]*$.}}
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
