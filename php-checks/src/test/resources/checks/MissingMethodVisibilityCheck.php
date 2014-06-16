<?php

abstract class C1
{
    static function f() { return; }          // NOK
    abstract function g();                   // NOK
    function h() {return; }                  // NOK

    private static function i() { return; }  // OK
    protected abstract function j();         // OK
    public function k() { return; }          // OK
}

class C2 {

  function C2 () { return; }
  function __destruct() { return; }
}

class C3 {
  function __construct() { return; }
}
