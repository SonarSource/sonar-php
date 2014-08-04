<?php

class Foo {
}

class Bar {

    public static function isFoo($foo) {
        return $foo instanceof Foo;
    }
}
