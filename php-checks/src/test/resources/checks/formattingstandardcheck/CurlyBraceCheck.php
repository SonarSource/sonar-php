<?php

/*
 *  Class and function
 */

class C1 {          // NOK
    function f() {  // NOK
    }

    function g()    // OK
    {
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
{                         // NOK
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
else {                  // NOK
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