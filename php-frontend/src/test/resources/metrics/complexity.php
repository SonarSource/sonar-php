<?php

function f2($p) {          // +1 function declaration

  $a = function($p) {};    // +1 function expression

  if ($a) {                 // +1 if statement
    throw new Exception();  // +1 throw exception
  }

  if ($a):                  // +1 alternative if statement
     ;                      // +0 empty statement
  endif;

  for (; ; ) {              // +1 for statement
  }

  foreach ($a as $b) {      // +1 foreach statement
    continue;
  }

  while ($a) {              // +1 while statement
  }

  do {                      // +1 do while statement
  } while ($a);

  switch ($a) {             // +0 switch statement
    case 0:                 // +1 case statement
      return $a;            // +1 return statement
    default:                // +0 default statement
      break;
  }

  try {
  } catch (Exception $e) {  // +1 catch statement
  }

 a:
    a ? b + 1 && c - 1 : d * 1 || e / 1; // +3

  goto a;                   // +1 goto statement

  return;                   // +0 last return in method
}

class A {

  public function f($a) {   // +1 method declaration
      return;                 // +0 last return in method
  }
}
