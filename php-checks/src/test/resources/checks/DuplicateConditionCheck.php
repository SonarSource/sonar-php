<?php
if ($x == 1) {
} elseif ($x == 2) {
} elseif ($x == 1) { // NOK {{This branch duplicates the one on line 2.}}
} elseif ($x == 1) { // NOK {{This branch duplicates the one on line 2.}}
}

if ($x == 1) {
} else if ($x == 2) {
} else if ($x == 2) { // NOK {{This branch duplicates the one on line 9.}}
}

switch($i) {
  case 1:
    break;
  case 3:
    break;
  case 1:  // NOK {{This case duplicates the one on line 14.}}
    break;
  default:
    break;
}
