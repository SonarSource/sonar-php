<?php

/*
 *  Class and function
 */

abstract class C1 { // Noncompliant {{Move this open curly brace to the beginning of the next line.}}
//                ^
    function f() {  // Noncompliant
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

interface I {       // Noncompliant
}

trait T {           // Noncompliant
}

function f() {      // Noncompliant
}

function g($p1, $p2,
           $p3, $p4
) {
}

/*
 *  Control structure
 */

if ($a)
  {                         // Noncompliant {{Move this open curly brace to the end of the previous line.}}
//^
}

do
{                         // Noncompliant

} while ($a);

while ($a)
{                         // Noncompliant
}

for ($i = 0; $i < 3; $i++)
{                         // Noncompliant
}

foreach ($array as $val)
{                         // Noncompliant
}

switch ($a)
{                         // Noncompliant
}

try
{                         // Noncompliant
} catch (Exception $e)
{                         // Noncompliant
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
  else {                  // Noncompliant {{Move this "else" to the same line as the previous closing curly brace.}}
//^^^^
}

try {
}
catch (Exception $e) {  // Noncompliant
}
finally {               // Noncompliant
}

if ($a) {
} else {                // OK
}
