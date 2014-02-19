<?php

switch ($variable) {    // NOK
  case 0:
    do_something();
    break;
  default:
    do_something_else();
    break;
}

switch ($variable) {    // NOK
}

switch ($variable):     // NOK
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
