<?php

namespace Package;

class ClassName {
    function test() {
        throw new \Exception();         // NOK {{Define and throw a dedicated exception instead of using a generic one.}}
//                ^^^^^^^^^^
    }
    function test() {
        throw new \RuntimeException();  // NOK
    }
    function test() {
        throw new \ErrorException();    // NOK
    }
}
