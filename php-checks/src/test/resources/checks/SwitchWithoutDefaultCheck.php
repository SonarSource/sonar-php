<?php

switch ($a) {
  case 0:
    break;
  default:    // OK
    break;
}

switch ($a) { // NOK {{Add a "case default" clause to this "switch" statement.}}
  case 1:
    break;
}

switch ($a) {
  case 0:
    break;
  default:    // NOK {{Move this "case default" clause to the end of this "switch" statement.}}
    break;
  case 1:
    break;
}
