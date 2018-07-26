<?php

switch ($a) {
}

switch ($a) {
  case 0:
    echo "";
    break;
}

switch ($a) {
  case 0:
    echo "";
    break;
  case 1:
    echo "";
    break;
  default:
    echo "";
    break;
}

switch ($a) {
  case 0:
    echo "";
    break;
  default: // Noncompliant {{Move this "default" clause to the beginning or end of this "switch" statement.}}
//^^^^^^^
    echo "";
    break;
  case 1:
    echo "";
    break;
}

switch ($a) {
  default:
    echo "";
    break;
  case 0:
    echo "";
    break;
  case 1:
    echo "";
    break;
}

switch ($a) {
  case 0:
    echo "";
    break;
  default: // Noncompliant
  case 1:
    echo "";
    break;
}

switch ($a) {
  case 0:
    echo "";
    break;
  case 1:
  default:
    echo "";
    break;
}


switch ($a):
  case 0:
    echo "";
    break;
  default:
    echo "";
    break;
endswitch;

switch ($a):
  case 0:
    echo "";
    break;
  default: // Noncompliant
    echo "";
    break;
  case 0:
    echo "";
    break;
endswitch;
