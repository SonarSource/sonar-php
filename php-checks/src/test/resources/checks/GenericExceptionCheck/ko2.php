<?php

namespace Package;

class ClassName {
    function test() {
        throw new \Exception();         // Noncompliant {{Define and throw a dedicated exception instead of using a generic one.}}
//                ^^^^^^^^^^
    }
    function test() {
        throw new \RuntimeException();  // Noncompliant
    }
    function test() {
        throw new \ErrorException();    // Noncompliant
    }
}
