<?php

$a = 1;
$b = 1;

echo $a;             // use global $a

$f = function () use ($a, &$b) {
    echo $a;         // use local $a
    echo $b;         // use global $b
};

function g() {
    global $a;
    echo $a;         // use global $a
}

function h($a) {
    echo $a;         // use parameter $a
}

function j() {
    $a = 1;
    echo $a;         // use local $a
}

$f();
h(j());

$array = [1, 2, 3];
echo $array[0];      // use global $array

class A {

    public $field;

    public function method() {
        $field = 1;
        echo $field;
        echo $this->field;
        $this->method();
    }
}


// compound variable
$compoundVar = "str";
${$compoundVar} = 1;    // use of $compoundVar
${compoundVar} = 1;     // use of $compoundVar

$var = "str";
echo "${var}";          // use of $var

