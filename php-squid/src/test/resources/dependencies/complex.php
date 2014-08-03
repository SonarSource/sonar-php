<?php

namespace A;

class One { }
class Two { }

function fn1() {
    return new One();
}

namespace B;

class Three { }
class Four { }

function fn2() {
    return new \A\Two();
}

namespace C;

function fn3(\A\One $one) {
    return $one;
}