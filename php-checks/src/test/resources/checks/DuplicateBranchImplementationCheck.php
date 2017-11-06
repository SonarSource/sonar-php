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

if ($a) { // OK, covered by S3923
  foo();
} else {
  foo();
}

switch($i) {
  case 1:
    doX();
    break;
  case 2:
  	doY();
    break;
  case 3:
  	doX();               // Noncompliant {{This case's code block is the same as the block for the case on line 27.}}
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

switch($a) { // OK, covered by S3923
  case 1:
    foo();
    break;
  case 2:
    foo();
    break;
  default:
    foo();
    break;
}

$condition ? foo1() : foo1();   // OK, covered by S3923
