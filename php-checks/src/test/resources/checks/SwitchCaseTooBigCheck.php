<?php

switch ($a) {
  case 0:      // OK
    echo "";
    echo "";
    break;
  case 1:      // Noncompliant {{Reduce this "switch/case" number of lines from 5 to at most 4, for example by extracting code into function.}}
//^^^^
    echo "";
    echo "";
    echo "";
    break;
  case 2:
  default:     // Noncompliant
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
  default:     // Noncompliant
//^^^^^^^
    echo "";
    echo "";
    echo "";
    break;
endswitch;
