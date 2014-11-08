<?php

class ClassName {
    function test() {
        throw new InvalidArgumentException();
    }
    function test() {
        throw new Package\Exception();
    }
    function test($error) {
        throw $error;
    }
    function test($className) {
        throw new $className;
    }
}
