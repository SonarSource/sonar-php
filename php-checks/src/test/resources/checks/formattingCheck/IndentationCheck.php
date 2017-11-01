<?php

/**
 * Function calls arguments indentation
 */
doSomething($p1,             // Noncompliant {{Either split this list into multiple lines, aligned at column "4" or put all arguments on line "6".}}
    $p2
);

doSomething(
    $p1, $p2                 // Noncompliant {{Either split this list into multiple lines, aligned at column "4" or put all arguments on line "10".}}
//  ^^^
);

doSomething(
    $p1,                     // Noncompliant {{Align all arguments in this list at column "4".}}
   $p2);                     // Noncompliant {{Move the closing parenthesis on the next line.}}

doSomething($p1, something(  // Noncompliant
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
function f($p1,         // Noncompliant {{Either split this list into multiple lines, aligned at column "4" or put all arguments on line "51".}}
           $p2
) {
}

function g(
    $p1, $p2            // Noncompliant {{Either split this list into multiple lines, aligned at column "4" or put all arguments on line "56".}}
) {
}

function h(
   $p1,                 // Noncompliant {{Align all arguments in this list at column "4".}}
    $p2)                // Noncompliant {{Move the closing parenthesis with the opening brace on the next line.}}
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

/**
 * Implement list indentation
 */

class C1 implements A,    // Noncompliant {{Either split this list into multiple lines or move it on the same line "81".}}
//                  ^
                    B
{}

class C2 implements
        A, B              // Noncompliant {{Either split this list into multiple lines or move it on the same line "86".}}
//      ^
{}

class C3 implements
    A,                    // Noncompliant {{Align all interfaces in this list at column "4".}}
     B
{}

class C4 implements A, B  // OK
{}

class C5 implements       // OK
    A,
    B
{}


// Arrays and anonymous functions are allowed to be split on lines

doSomething($a, [      // OK
  1,
  2], $b);

doSomething($a, function () {      // OK
  // ...
  }, $b);

doSomething(a, function () { // Noncompliant {{Either split this list into multiple lines, aligned at column "4" or put all arguments on line "115".}}
  // ...
  },
  b
);

// not an array or function
doSomething($a, 1           // Noncompliant {{Either split this list into multiple lines, aligned at column "4" or put all arguments on line "122".}}
   + 2, $b
);

doSomething(        // OK
    $a,
    function () {

    },
    $b
);


doSomething($a, array(      // OK
  1,
  2), $b);

doSomething($a, array(      // OK
  1,
  2
), $b);
