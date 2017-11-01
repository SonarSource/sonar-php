<?php

class Foo
{
    var $foo = 1;     // Noncompliant {{Replace the "var" keyword with the modifier "public".}}
//  ^^^
    public $bar = 1;  // OK
}
