<?php

function f($p1, $p2 = 2, $p3) {           // NOK {{Move arguments "$p2" after arguments without default value}}
  return;
}

function f($p1 = 1, $p2, $p3 = 3, $p4) {   // NOK {{Move arguments "$p1", "$p3" after arguments without default value}}
  return;
}

function g($p1, $p2 = 2) {               // OK
  return;
}
