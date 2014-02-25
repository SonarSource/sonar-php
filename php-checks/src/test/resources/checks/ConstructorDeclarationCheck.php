<?php

class C {

  public function C() {             // NOK
  }

  public function f() {
  }
}


class C {

  const C = 1;

  public function __constructor() { // OK
  }

  public function f() {
  }
}
