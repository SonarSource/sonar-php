<?php

class Foo
{
    private $bar = 1, $bar2 = 2;       // NOK
    const BAR1 = 1, BAR2 = 2;  // NOK

    private $foo = 1;                  // OK
    const FOO = 1;             // OK
}