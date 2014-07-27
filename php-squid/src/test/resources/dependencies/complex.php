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
