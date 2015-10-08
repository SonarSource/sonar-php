<?php

$a = $b and $c; // NOK  {{Replace "and" with "&&".}}
$a = $b or $c;  // NOK  {{Replace "or" with "||".}}

$a = $b && $c;  // OK
$a = $b || $c;  // OK


