<?php

function f($p1, $p2 = 2, $p3) {           // NOK
  return;
}

function f($p1 = 1, $p2, $p3 = 3, $p4) {   // NOK
  return;
}

function g($p1, $p2 = 2) {               // OK
  return;
}
