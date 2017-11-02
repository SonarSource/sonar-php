<?php

   switch ($variable) {    // Noncompliant {{Replace this "switch" statement with "if" statements to increase readability.}}
// ^^^^^^
  case 0:
    do_something();
    break;
  default:
    do_something_else();
    break;
}

switch ($variable) {    // Noncompliant
}

switch ($variable):     // Noncompliant
  case 0:
    do_something();
    break;
  default:
    do_something_else();
    break;
endswitch;

switch ($a) {           // OK
  case 0:
    do_something();
    break;
  case 0:
  default:
    do_something_else();
    break;
}
