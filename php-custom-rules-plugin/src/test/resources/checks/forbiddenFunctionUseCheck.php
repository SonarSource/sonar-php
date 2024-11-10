<?php

foo();                  //Noncompliant {{Remove the usage of this forbidden function.}}
foo(1);                 //Noncompliant

bar();                  //Noncompliant
bar(1);                 //Noncompliant

class Obj
{
    public function foo()
    {
        return "foo";
    }

    public function bar()
    {
        return "bar";
    }
}

$myObj = new Obj();

$myObj->foo();            // OK
$myObj->bar();            // OK
