<?php

namespace Package;

class ClassName {
    function test1() {
        throw new \InvalidArgumentException();
    }

    function test2() {
        throw new Exception();
    }
}
