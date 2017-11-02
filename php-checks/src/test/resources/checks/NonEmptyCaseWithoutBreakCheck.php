<?php

switch ($a) {
  case 0:
  case 1:     // Noncompliant {{End this switch case with an unconditional break, continue, return or throw statement.}}
//^^^^^^^
    doSomething();
  case 2:     // Noncompliant
    halt();
  case 3:     // OK
    echo "";
    // no break intentional
  default:    // OK
    doSomethingElse();
}

switch ($a) {
  default:     // Noncompliant
    doSomething();
  case 2:      // OK
    doSomethingElse();
}

switch ($a) {
  case 0:      // OK
  case 1:      // OK
    break;
  case 2:      // OK
    return;
  case 3:      // OK
    throw new Exception();
  case 4:      // OK
    continue;
  case 5:      // OK
    exit(0);
  case 6:      // OK
    goto myLabel;
  default:     // OK
    break;
}

//Support alternate switch syntax
switch ($a):
  case 0:
  case 1:     // Noncompliant
    doSomething();
  case 2:     // Noncompliant
    halt();
  case 3:     // OK
    echo "";
    // this is intentional
  default:    // OK
    doSomethingElse();
endswitch;

switch ($a) {
  case 0:
    if ($b) {
      throw anException();
    } else {
      throw otherException();
    }
  default:
    break;
}
