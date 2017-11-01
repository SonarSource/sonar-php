<?php

$a = 1;
  $a == 1;   // Noncompliant {{Remove or refactor this statement.}}
//^^^^^^^^

$b = $a < 2;
$a < 2;    // Noncompliant

$funcExpr = function() {};
function() {};    // Noncompliant

echo "str";
"str";            // Noncompliant
"str{$a}";        // Noncompliant

$a."str";         // Noncompliant

$a != 1;  // Noncompliant
$a === 1; // Noncompliant
$a !== 1; // Noncompliant

?>
