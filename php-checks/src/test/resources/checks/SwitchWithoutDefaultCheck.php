<?php

switch ($a) {
  case 0:
    break;
  default:    // OK
    break;
}

switch ($a) { // NOK
  case 1:
    break;
}

switch ($a) {
  case 0:
    break;
  default:    // NOK
    break;
  case 1:
    break;
}
