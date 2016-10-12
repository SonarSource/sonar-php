<?php
if ($x == 1) {
	doX();
} elseif ($x == 2) {
	doY();
} elseif ($x == 3) {  doX(); } // NOK  [[secondary=-4]] {{This branch's code block is the same as the block for the branch on line 2.}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^
else {                // NOK {{This branch's code block is the same as the block for the branch on line 2.}}
	doX();
}

if ($x == 1) {
	doX();
} else if ($x == 2) {
	doY();
} else if ($x == 3) {                       // NOK {{This branch's code block is the same as the block for the branch on line 14.}}
	doY();
}

switch($i) {
  case 1:
    doX();
    break;
  case 2:
  	doY();
    break;
  case 3:               // NOK {{This case's code block is the same as the block for the case on line 21.}}
  	doX();
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

$condition ? foo1() : foo1();   // NOK {{This conditional operation returns the same value whether the condition is "true" or "false".}}


$condition ? $nestedCondition ? foo1() : foo2() : $nestedCondition ? foo1() : foo2(); // NOK
$condition ? $nestedCondition ? foo1() : foo1() : foo2(); // NOK

false ? false ? foo(3) : foo(4) : foo(5);   // OK
false ? false ? foo(1) : foo(1) : foo(5);   // NOK


false
   ? false       // NOK
      ? foo($b)
      : foo($b)
   : foo(5);
