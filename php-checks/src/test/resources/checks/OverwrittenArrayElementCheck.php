<?php

$a[2] = "foo";
$a[2] = "bar"; // Noncompliant

$b[1] = "foo";
$b[2] = "bar"; // Compliant

$c[1] = "foo";
doSomething($c[1]);
$c[1] = "bar"; // Compliant


$d[1] = "foo";
if (FOO) {
  echo $d[1];
}
$d[1] = "bar"; // Compliant

$e[1] = "foo";
echo $e[$x]; // We do not know what $x is
$e[1] = "bar";

