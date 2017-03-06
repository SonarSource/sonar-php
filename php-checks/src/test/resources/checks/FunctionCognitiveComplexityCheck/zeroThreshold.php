<?php

  function ko() // NOK [[effortToFix=13]] {{Refactor this function to reduce its Cognitive Complexity from 13 to the 0 allowed.}}
//^^^^^^^^
{
  switch (foo) // +1
  {
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    default:
    ;
  }

  if (true) { // +1
    return $a && $b || $c && $d || $e && $f ; // +5
  } else {    // +1
    return $a && $b && $c && $d && $e; // +1
  }

  if (true) { // +1
    return 1;
  }

  while ($a) { // +1 (+1 nesting)
    if (false) { // +2
      throw new Exception(); // +0
    }
  }
  return 1;
}

function ok() {
}

class C {

  public function ko() // NOK [[effortToFix=1]]
  {
    switch (foo) // +1
    {
      case 1:
      case 2:
      default:
      ;
    }
  }

  public function ok() {
  }
}

$f = function() { // NOK [[effortToFix=2]] [[secondary=+2,+3]]
//   ^^^^^^^^
  if (true) {
    return a && b;
  }
};

function nesting() { // NOK [[effortToFix=3]] [[secondary=+2,+4]]
  $nested = function() {  // (+1 nesting)
    if ($a) {} // +2
  };
  if ($a) {} // +1
}
