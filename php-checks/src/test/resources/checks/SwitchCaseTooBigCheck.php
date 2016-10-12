<?php

switch ($a) {
  case 0:      // OK
    echo "";
    echo "";
    break;
  case 1:      // NOK {{Reduce this "switch/case" number of lines from 6 to at most 4, for example by extracting code into function.}}
//^^^^
    echo "";
    echo "";
    echo "";
    break;
  case 2:
  default:     // NOK
    echo "";
    echo "";
    echo "";
    break;
}

switch ($a) {
}

switch ($a):
  case 0:      // OK
    echo "";
    echo "";
    break;
  default:     // NOK
//^^^^^^^
    echo "";
    echo "";
    echo "";
    break;
endswitch;
