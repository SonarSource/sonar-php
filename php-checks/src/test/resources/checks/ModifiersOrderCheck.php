<?php

abstract class Foo
{
    static private $foo1;   // Noncompliant {{Reorder the modifiers to comply with the PSR2 standard.}}
//  ^^^^^^^^^^^^^^
    static protected $foo2; // Noncompliant
    private static $foo3;   // OK

    public abstract function f1();         // Noncompliant
    abstract public function f2();         // OK
    public final function f3() { return; } // Noncompliant
    final public function f4() { return; } // OK
    
    public $x = 1; // OK
    var $y = 1;    // OK

    // PHP asymmetric visibility
    public private(set) string $name; // OK
    abstract public protected(set) string $abstractName { get; set; } // OK
    public private(set) readonly string $readonlyName; // OK
    PUBLIC PRIVATE(SET) string $upperCaseName; // OK
    private(set) public string $misorderedName; // Noncompliant
    // Asymmetric visibility for static properties is supported since PHP 8.5.
    public private(set) static string $staticName; // OK
    public static private(set) string $misorderedStaticName; // Noncompliant
}
