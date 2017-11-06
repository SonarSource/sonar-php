<?php
if ($x == 1) { doX(); }
//           ^^^^^^^^^^>
elseif ($x == 2) { doY(); }
elseif ($x == 3) { doX(); } // Noncompliant  {{This branch's code block is the same as the block for the branch on line 2.}}
//               ^^^^^^^^^^
else {                // Noncompliant {{This branch's code block is the same as the block for the branch on line 2.}}
	doX();
}

if ($x == 1) {
	doX();
} else if ($x == 2) {
	doY();
} else if ($x == 3) {                       // Noncompliant {{This branch's code block is the same as the block for the branch on line 13.}}
	doY();
}

switch($i) {
  case 1:
    doX();
    break;
  case 2:
  	doY();
    break;
  case 3:
  	doX();               // Noncompliant {{This case's code block is the same as the block for the case on line 21.}}
    break;
  case 4:
  case 5:
  case 6:
    doA();
  case 7:
    break;
  default:
    break;
}

$condition ? foo1() : foo1();   // Noncompliant {{This conditional operation returns the same value whether the condition is "true" or "false".}}


$condition ? ($nestedCondition ? foo1() : foo2()) : ($nestedCondition ? foo1() : foo2()); // Noncompliant
$condition ? ($nestedCondition ? foo1() : foo1()) : foo2(); // Noncompliant

false ? false ? foo(3) : foo(4) : foo(5);   // OK
false ? false ? foo(1) : foo(1) : foo(5);   // Noncompliant


false
   ? false       // Noncompliant
      ? foo($b)
      : foo($b)
   : foo(5);
