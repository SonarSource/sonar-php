<?php

namespace Package;

use Exception;
use RuntimeException;
use ErrorException;
use Exception as Alias1;
use RuntimeException as Alias2;
use ErrorException as Alias3;
use Foo\SomeException as Alias4;
use Foo\Exception as Alias5;

class ClassName {
    function test() {
        throw new Exception();          // Noncompliant {{Define and throw a dedicated exception instead of using a generic one.}}
//                ^^^^^^^^^
    }
    function test() {
        throw new RuntimeException();   // Noncompliant
    }
    function test() {
        throw new ErrorException();     // Noncompliant
    }
    function test() {
        throw new Alias1();             // Noncompliant
    }
    function test() {
        throw new Alias2();             // Noncompliant
    }
    function test() {
        throw new Alias3();             // Noncompliant
    }
    function test() {
        throw new Alias4();             // OK
    }
    function test() {
        throw new Foo\Alias3();         // OK
    }
    function test() {
        throw new Alias4();         // OK
    }
}
