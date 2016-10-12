<?php

abstract class Foo
{
    static private $foo1;   // NOK {{Reorder the modifiers to comply with the PSR2 standard.}}
//  ^^^^^^^^^^^^^^
    static protected $foo2; // NOK
    private static $foo3;   // OK

    public abstract function f1();         // NOK
    abstract public function f2();         // OK
    public final function f3() { return; } // NOK
    final public function f4() { return; } // OK
    
    public $x = 1; // OK
    var $y = 1;    // OK
}
