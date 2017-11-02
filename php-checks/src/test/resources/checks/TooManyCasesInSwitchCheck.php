<?php

  switch ($i) { // Noncompliant {{Reduce the number of switch cases from 4 to at most 3.}}
//^^^^^^
    case 0:
    case 1:
      break;
    case 2:
      break;
    default:
      break;
}

switch ($i) {
    case 0:
    case 1:
      doSomething();
    default:
      break;
}

switch ($i) {
}
