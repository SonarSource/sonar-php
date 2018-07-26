<?php
if ($x == 1) {
//           ^[el=+4;ec=1]>
  doX();
  doY(); 
}
elseif ($x == 2) { doY(); }
elseif ($x == 3) { // Noncompliant {{This branch's code block is the same as the block for the branch on line 2.}}
//               ^[el=+4;ec=1]
  doX();
  doY();
}
else {  // Noncompliant {{This branch's code block is the same as the block for the branch on line 2.}}
  doX();
  doY();
}

if ($x == 1) {
  doX();
  doZ();
} else if ($x == 2) {
  doY();
} else if ($x == 3) { // Noncompliant {{This branch's code block is the same as the block for the branch on line 18.}}
  doX();
  doZ();
}

if ($a) { // OK, covered by S3923
  foo();
} else {
  foo();
}

switch($i) {
  case 1:
    doX();
    doZ();
    break;
  case 2:
    doY();
    break;
  case 3:
    doX();               // Noncompliant {{This case's code block is the same as the block for the case on line 36.}}
    doZ();
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

if(a == 1) {
  doSomething();  //no issue, usually this is done on purpose to increase the readability
} else if (a == 2) {
  doSomethingElse();
} else {
  doSomething();
}

if(a == 1) {
  doSomething();
} else if (a == 2) { // Noncompliant
  doSomething();
}

// SONARPHP-782

if(a == 1) {
  doSomething();
} else if (a == 2) { // Noncompliant
  doSomething();
} else if (a == 3) { // Noncompliant
  doSomething();
}

if ($a >= 0 && $a < 10) {
  doTheThing();
}
else if ($a >= 10 && $a < 20) {
  doTheOtherThing();
}
else if ($a >= 20 && $a < 50) {
  doTheThing();  // no issue, usually this is done on purpose to increase the readability
}

switch($a) {
  case 1:
    doTheThing();
    break;
  case 2:
    doTheOtherThing();
    break;
  case 3:
    doTheThing();  // no issue
    break;  
}

switch($a) {
  case 1:
    doTheThing();
  case 2:
    doTheOtherThing();
  case 3:
    doTheThing();  // no issue, one line
}

switch($a) {
  case 1:
    doTheThing();
    break;
  case 2:
    doTheThing(); //Noncompliant
    break;
  case 3:
    doTheThing();  // Noncompliant
    break;  
}

switch($a) {
  case 1:
    doTheThing();
  case 2:
    doTheThing(); // Noncompliant
  case 3:
    doTheThing();  // Noncompliant  
}

switch($a) {
  case 1:
    doTheThing();
    A();
  case 2:
    doTheThing();
    A();
  case 3:
    doTheThing();
    A();
  default:
    doTheThing();
    A();
}