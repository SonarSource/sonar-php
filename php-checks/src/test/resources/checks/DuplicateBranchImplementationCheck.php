<?php
if ($x == 1) {
	doX();
} elseif ($x == 2) {
	doY();
} elseif ($x == 3) { // Noncompliant
	doX();
} else { // Noncompliant
	doX();
}

if ($x == 1) {
	doX();
} else if ($x == 2) {
	doY();
} else if ($x == 3) { // Noncompliant
	doY();
}

switch($i) {
  case 1:
    doX();
    break;
  case 2:
  	doY();
    break;
  case 3:  // Noncompliant
  	doX();
    break;
  default:
    break;
}
