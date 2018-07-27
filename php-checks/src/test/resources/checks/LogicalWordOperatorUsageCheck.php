<?php

$a = $b and $c; // Noncompliant  {{Replace "and" with "&&".}}
//      ^^^
$a = $b or $c;  // Noncompliant  {{Replace "or" with "||".}}

$a = $b && $c;  // OK
$a = $b || $c;  // OK

$a = $b or die("error");  // OK
$a = $b or foo("error");  // Noncompliant
