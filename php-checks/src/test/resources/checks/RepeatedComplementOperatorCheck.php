<?php

$c = !!$a; // Noncompliant {{Use the "!" operator just once or not at all. If a type cast is intended, use "(bool)" instead.}}
$d = ~~$b; // Noncompliant {{Use the "~" operator just once or not at all.}}

$c = !(!$a); // Noncompliant
$d = ~(~$b); // Noncompliant

$c = !((!$a)); // Noncompliant
$d = ~((~$b)); // Noncompliant
$c = !(((!$a))); // Noncompliant

$c = !$a; // Compliant
$d = ~$b; // Compliant

$c = !(!$a && $e); // Compliant
$d = ~(~$b && $e); // Compliant
$d = ~($b && ~$e); // Compliant
$c = !!$a && $b; // Noncompliant

$c = !$a <=> $b; // Compliant
$c = !$a & $b; // Compliant
$c = !$a ?? $b; // Compliant

$c = --$b; // Compliant
$c = -(-$b); // Noncompliant {{Use the "-" operator just once or not at all. If a type cast is intended, use "(int)" instead.}}
$c = -(--$b); // Compliant
$c = -(-$b && $c); // Compliant
