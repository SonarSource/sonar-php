<?php

function f1($param) {     // OK

  $local1 = function () { // OK
    $innerLocal = 1;      // OK
  };

  $local2 =& $local1;
}


function f1($PARAM) {     // NOK

  $LOCAL = function () {  // NOK
    $INNER_LOCAL = 1;     // NOK
    $LOCAL = 1;           // NOK
  };

  $LOCAL = 2;             // OK - already declared
}

class C {

  private $a;

  function f() {
    $LOCAL = 1;           // NOK
    $this->a = 1;         // OK
  }
}

$GLOBAL_VAR = 1;          // OK

function f2() {
  $_GET = array();        // OK
}
