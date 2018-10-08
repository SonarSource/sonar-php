<?php

// functions are case-insensitive
function myFunc(){}
MyFunc();
MYFUNC();

// variables and constants are case sensitive

const myconst = 1;
const MYCONST = 2;
echo MYCONST;

$myvar = 1;
$MYVAR = 2;
echo $myvar;

function foo($p, $P) {
    echo $p;
    echo $P;
    echo $P;
}
