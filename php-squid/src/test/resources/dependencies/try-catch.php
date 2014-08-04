<?php

class A {

    public static function foo() {
        try {
            bar();
        } catch (RuntimeError $e) {
            echo $e;
        }
    }
}
