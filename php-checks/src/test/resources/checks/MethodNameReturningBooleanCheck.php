<?php

class A {

  /**
   *  @return boolean
   */
  public function getA() {    // Noncompliant {{Rename this method to start with "is" or "has".}}
//                ^^^^
  }

  /**
   *  @return bool
   */
  public function getB() {    // Noncompliant
  }

  public function getC() {    // OK
  }

  /**
   *  @return boolean
   */
  public function isA() {     // OK
  }

  /**
   *  @return bool
   */
  public function hasB() {    // OK
  }

}

