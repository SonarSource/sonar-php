<?php

  function ko() // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 13 to the 0 allowed.}} [[effortToFix=13]]
//^^^^^^^^
{
  switch (foo)
//^^^^^^< {{+1}}
  {
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    default:
    ;
  }

  if (true) {
//^^< {{+1}}
    return $a && $b      ||      $c && $d      ||      $e && $f ;
//            ^^< {{+1}} ^^< {{+1}} ^^< {{+1}} ^^< {{+1}} ^^< {{+1}}
  } else {
//  ^^^^< {{+1}}
    return $a && $b && $c && $d && $e;
//            ^^< {{+1}}
  }

  if (true) {
//^^< {{+1}}
    return 1;
  }

  while ($a) {
//^^^^^< {{+1}}
    if (false) {
//  ^^< {{+2 (incl. 1 for nesting)}}
      throw new Exception(); // +0
    }
  }
  return 1;
}

function ok() {
}

class C {

  public function ko() // Noncompliant [[effortToFix=1]]
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

$f = function() { // Noncompliant [[effortToFix=2]]
//   ^^^^^^^^
  if (true) {
//^^<
    return a && b;
//           ^^< {{+1}}
  }
};

  function nesting() { // Noncompliant [[effortToFix=3]]
//^^^^^^^^
  $nested = function() {
    if ($a) {}
//  ^^< {{+2 (incl. 1 for nesting)}}
  };
  if ($a) {}
//^^< {{+1}}
}
