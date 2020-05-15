<?php

$a[2] = "foo";
$a[2] = "bar"; // Noncompliant

$b[1] = "foo";
$b[2] = "bar"; // Compliant

$c[1] = "foo";
doSomething($c[1]);
$c[1] = "bar";
