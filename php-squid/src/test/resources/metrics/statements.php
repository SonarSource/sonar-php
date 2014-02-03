<?php

namespace NS;

use myNamespace as NS;    // +1 se statement

global $a;                // +1 global statement

const A = 1;              // +1 const declaration

if ($a) {                 // +1 if statement
  throw new Exception();  // +1 throw exception
}

if ($a):                  // +1 alternative if statement
   ;                      // +1 empty statement
endif;

for (; ; ) {              // +1 for statement
  break;                  // +1 break statement
}

foreach ($a as $b) {      // +1 foreach statement
  continue;               // +1 continue statement
}

while ($a) {              // +1 while statement
}

do {                      // +1 do while statement
} while ($a);

switch ($a) {             // +1 switch statement
  case 0:
    return $a;            // +1 return statement
}

try {                     // +1 try statement
} catch (Exception $e) {
}

a:                        // +1 label statement
  unset ($a);             // +1 unset statement

goto a;                   // +1 goto satement

$a = 1;                   // +1 expression statement

declare(ticks=1)          // +1 declare statement

echo $a;                  // +1 echo statement

static $a;                // +1 static statement

function f($p) {
  yield $p;               // +1 yield statement
}

__halt_compiler();        // +1 halt compiler statement

class C {
  use myTrait;            // +1 use trait statement
  const B = 1;            // +1 class const declaration
  private $a;             // +1 class variable declaration
}

