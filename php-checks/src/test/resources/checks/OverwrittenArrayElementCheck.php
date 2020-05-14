<?php

$a[1] = "foo";
$a[2] = "bar"; // Noncompliant

$b[1] = "foo";
echo $b[1];
$b[2] = "bar"; // Compliant
