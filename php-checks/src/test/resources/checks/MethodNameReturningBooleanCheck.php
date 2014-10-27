<?php

class A {

  /**
   *  @return boolean
   */
  public function getA() {    // NOK
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

