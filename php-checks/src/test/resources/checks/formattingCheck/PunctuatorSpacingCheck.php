<?php

/**
 * Space between closing parenthesis and opening curly brace
 */
if ($a){           // NOK {{Put one space between the closing parenthesis and the opening curly brace.}}

} else if ($b)  {  // NOK {{Put only one space between the closing parenthesis and the opening curly brace.}}

}

if ($c) {          // OK

}

function f()       // OK
{
}

/**
 * Spacing inside parenthesis
 */

doSomething( $p1, $p2);   // NOK {{Remove all space after the opening parenthesis.}}
doSomething($p1, $p2 );   // NOK {{Remove all space before the closing parenthesis.}}
doSomething( $p1, $p2 );  // NOK {{Remove all space after the opening parenthesis and before the closing parenthesis.}}
doSomething($p1, $p2);    // OK
doSomething(              // OK
    $p1, $p2);
doSomething($p1, $p2      // OK
);
doSomething(              // OK
    $p1, $p2
);

$flags = array( preg_match()); // NOK

"{$space1}{$space0}SUM($op)";