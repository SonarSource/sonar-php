<?php

class Foo
{
    private $bar = 1, $bar2 = 2;       // Noncompliant {{2 property declarations were found in this statement. Reformat the code to declare only one property per statement.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    const BAR1 = 1, BAR2 = 2;  // Noncompliant {{2 property declarations were found in this statement. Reformat the code to declare only one property per statement.}}

    private $foo = 1;                  // OK
    const FOO = 1;             // OK
}
