<?php

/**
 * Space between closing parenthesis and opening curly brace
 */
if ($a){           // NOK

} else if ($b)  {  // NOK

}

if ($c) {          // OK

}

function f()       // OK
{
}

/**
 * Space around arguments' comma
 */

function f($p1 , $p2, $p3, $p4) {}  // NOK - space before
function g($p1,$p2, $p3, $p4) {}    // NOK - no space after
function h($p1 ,$p2, $p3, $p4) {}   // NOK - space after & no space after
function i($p1 ,$p2, $p3 ,$p4) {}   // NOK - space after & no space after x2
doSomething($p1,$p2);               // NOK
function j($p1, $p2, $p3, $p4) {}   // OK

/**
 * No space after function's name
 */

function f () {}     // NOK
$a->doSomething ();  // NOK
function g() {}      // OK
doSomething();       // OK

/**
 * Spacing inside parenthesis
 */

doSomething( $p1, $p2);   // NOK
doSomething($p1, $p2 );   // NOK
doSomething( $p1, $p2 );  // NOK
doSomething($p1, $p2);    // OK
doSomething(              // OK
    $p1, $p2);
doSomething($p1, $p2      // OK
);
doSomething(              // OK
    $p1, $p2
);
