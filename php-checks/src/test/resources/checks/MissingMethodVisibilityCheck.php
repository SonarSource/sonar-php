<?php

abstract class C
{
    static function f() { return; }          // NOK
    abstract function g();                   // NOK
    function h() {return; }                  // NOK

    private static function i() { return; }  // OK
    protected abstract function j();         // OK
    public function k() { return; }          // OK
}
