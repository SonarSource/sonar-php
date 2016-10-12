<?php

$a = 1;
  $a == 1;   // NOK {{Remove or refactor this statement.}}
//^^^^^^^^

$b = $a < 2;
$a < 2;    // NOK

$funcExpr = function() {};
function() {};    // NOK

echo "str";
"str";            // NOK
"str{$a}";        // NOK

$a."str";         // NOK

$a != 1;  // NOK
$a === 1; // NOK
$a !== 1; // NOK

?>
