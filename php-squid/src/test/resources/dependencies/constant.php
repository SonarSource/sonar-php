<?php

class A {
    const TEST = 'bar';
}

class B {

    public static function foo() {
        return A::TEST;
    }
}
