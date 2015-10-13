<?php

/*
 *  Class and function
 */

abstract class C1 { // NOK {{Move this open curly brace to the beginning of the next line.}}
    function f() {  // NOK
    }

    function g()    // OK
    {
    }

    function h();   // OK

    function g($p1, $p2,
               $p3, $p4
    ) {              // OK
    }

}

class C2            // OK
{
}

interface I {       // NOK
}

trait T {           // NOK
}

function f() {      // NOK
}

function g($p1, $p2,
           $p3, $p4
) {
}

/*
 *  Control structure
 */

if ($a)
{                         // NOK {{Move this open curly brace to the end of the previous line.}}
}

do
{                         // NOK

} while ($a);

while ($a)
{                         // NOK
}

for ($i = 0; $i < 3; $i++)
{                         // NOK
}

foreach ($array as $val)
{                         // NOK
}

switch ($a)
{                         // NOK
}

try
{                         // NOK
} catch (Exception $e)
{                         // NOK
}

try {                     // OK
} catch (Exception $e) {  // OK
}

if ($aVeryLongConditionalExpression
) {                       // OK
}

/*
 * } and ELSE, CATCH, FINALLY
 */
if ($a) {
}
else {                  // NOK {{Move this "else" to the same line as the previous closing curly brace.}}
}

try {
}
catch (Exception $e) {  // NOK
}
finally {               // NOK
}

if ($a) {
} else {                // OK
}
