<?php

foo();                  // NOK {{Remove the usage of this forbidden function.}}
foo(1);                 // NOK

bar();                  // NOK
bar(1);                 // NOK

class Obj {
    function foo() {
        return "foo";
    }

    function bar() {
        return "bar";
    }
};

$myObj = new Obj();

$myObj->foo();            // OK
$myObj->bar();            // OK
