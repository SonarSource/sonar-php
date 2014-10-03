<?php

function __construct() {  // OK
}

function __destruct() {   // OK
}

function doSomething() {  // OK
}

function DoSomething() {  // NOK
}

class A {

  /**
   * @inheritdoc
   */
  public function DoSomething() {  // OK
  }

}
