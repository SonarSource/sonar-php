<?php

/**
 * Arguments indentation
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

