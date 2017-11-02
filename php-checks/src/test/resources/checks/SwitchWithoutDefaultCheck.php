<?php

switch ($a) {
  case 0:
    break;
  default:    // OK
    break;
}

  switch ($a) { // Noncompliant {{Add a "case default" clause to this "switch" statement.}}
//^^^^^^
  case 1:
    break;
}

  switch ($a) {
  case 0:
    break;
  default:    // Noncompliant {{Move this "case default" clause to the end of this "switch" statement.}}
//^^^^^^^
    break;
  case 1:
    break;
}
