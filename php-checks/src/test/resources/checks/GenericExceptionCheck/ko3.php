<?php

namespace Package;

use Exception;
use RuntimeException;
use ErrorException;
use Exception as Alias1;
use RuntimeException as Alias2;
use ErrorException as Alias3;

class ClassName {
    function test() {
        throw new Exception();
    }
    function test() {
        throw new RuntimeException();
    }
    function test() {
        throw new ErrorException();
    }
    function test() {
        throw new Alias1();
    }
    function test() {
        throw new Alias2();
    }
    function test() {
        throw new Alias3();
    }
}
