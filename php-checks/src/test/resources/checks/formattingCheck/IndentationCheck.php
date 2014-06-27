<?php

/**
 * Function calls arguments indentation
 */
doSomething($p1,             // NOK
    $p2
);

doSomething(
    $p1, $p2                 // NOK
);

doSomething(
    $p1,                     // NOK
   $p2);                     // NOK

doSomething($p1, something(  // NOK
    $p1,
    $p2,
    $p3,
    $p4
));

doSomething(
    $p1,                     // OK
    array(
        $p1,
        $p2
    ),
    $p2
);

doSomething($p1, $p2);       // OK

doSomething(                 // OK
    $p1,
    $p2
);

doSomething(anotherThing(    // OK
    $p1,
    $p2,
    $p2
));

/**
 * Method declaration argument indentation
 */
function f($p1,         // NOK
           $p2
) {
}

function g(
    $p1, $p2            // NOK
) {
}

function h(
   $p1,                 // NOK
    $p2)                // NOK - closing parenthesis
{
}

function j($p1, $p2)    // OK
{
}

function k(             // OK
    $p1,
    $p2
) {
}

