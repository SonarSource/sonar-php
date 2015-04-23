<?php
if ($x == 1) {
} elseif ($x == 2) {
} elseif ($x == 1) { // Noncompliant
} elseif ($x == 1) { // Noncompliant
}

if ($x == 1) {
} else if ($x == 2) {
} else if ($x == 2) { // Noncompliant
}

switch($i) {
  case 1:
    break;
  case 3:
    break;
  case 1:  // Noncompliant
    break;
  default:
    break;
}
