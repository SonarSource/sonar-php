<?php

function f1($param) {     // OK

  $local1 = function () { // OK
    $innerLocal = 1;      // OK
  };

  $local2 =& $local1;
}


function f1($PARAM) {     // Noncompliant {{Rename this parameter "$PARAM" to match the regular expression ^[a-z_][a-zA-Z0-9_]*$.}}
//          ^^^^^^

  $LOCAL = function () {  // Noncompliant {{Rename this local variable "$LOCAL" to match the regular expression ^[a-z_][a-zA-Z0-9_]*$.}}
//^^^^^^
    $INNER_LOCAL = 1;     // Noncompliant
    $LOCAL = 1;           // Noncompliant
  };

  $LOCAL = 2;             // OK - already declared
}

class C {

  private $a;

  function f() {
    $LOCAL = 1;           // Noncompliant
    $this->a = 1;         // OK
    $LOCAL2[0][1] = 1;    // Noncompliant
    $LOCAL3 += 1;         // Noncompliant
    $LOCAL4 =& $LOCAL;    // Noncompliant
    $local_var = 1;       // OK
  }
}

$GLOBAL_VAR = 1;          // OK

function f2() {
  $_GET = array();        // OK
}

$f = fn($param, $second_param) => $param * 2;
$f = fn($PARAM) => $PARAM * 2; // Noncompliant
