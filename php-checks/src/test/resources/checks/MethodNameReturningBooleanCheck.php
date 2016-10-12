<?php

class A {

  /**
   *  @return boolean
   */
  public function getA() {    // NOK {{Rename this method to start with "is" or "has".}}
//                ^^^^
  }

  /**
   *  @return bool
   */
  public function getB() {    // NOK
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

