<?php

$a = 0;
$b = false;

$c = !!$a; // Noncompliant
$d = ~~$b; // Noncompliant

$c = !(!$a); // Noncompliant
$d = ~(~$b); // Noncompliant

$c = !((!$a)); // Noncompliant
$d = ~((~$b)); // Noncompliant

$c = !$a; // Compliant
$d = ~$b; // Compliant

$c = !(!$a && $e); // Compliant
$d = ~(~$b && $e); // Compliant
