<?php

abstract class Foo
{
    static private $foo1;
    static protected $foo2;
    public static $foo3;

    public abstract function f();

    public final function g() { return; }

    abstract public function h();
}
