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
  default:
    break;
  case 1:
    break;
}

switch ($a) { // Noncompliant
}
