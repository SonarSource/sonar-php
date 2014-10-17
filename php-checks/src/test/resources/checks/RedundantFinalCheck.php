<?php

final class A {

  public final function f() {  // NOK
  }

  public function g() {        // OK
  }

}
