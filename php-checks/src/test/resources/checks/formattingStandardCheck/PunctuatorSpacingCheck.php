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
