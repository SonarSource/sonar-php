<?php

  $a[2] = "foo";
//^^^^^^^^^^^^^> {{Original assignment.}}
  $a[2] = "bar"; // Noncompliant {{Verify this is the array key that was intended to be written to; a value has already been saved for it and not used.}}
//^^^^^^^^^^^^^^

$a_1[1] = "foo";
$a_1[2] = "foo2";
$a_1[1] = "bar"; // Noncompliant

$b[1] = "foo";
$b[2] = "bar"; // Compliant

$c[1] = "foo";
doSomething($c[1]);
$c[1] = "bar"; // Compliant

$c_1[1] = "foo";
doSomething($c_1);
$c_1[1] = "bar"; // Compliant

$d[1] = "foo";
if (FOO) {
  echo $d[1];
}
$d[1] = "bar"; // Compliant

$e[1] = "foo";
echo $e[$x]; // We do not know what $x is
$e[1] = "bar";

$f[1] = "foo";
$f[1] = $f[1] . "bar"; // Compliant

$j[1] = "foo";
$bla = function($j) {
  $j[1] = "bar"; // Compliant
};

// Coverage
$z = "foo";
foo()[1] = "foo";
$z[] = "foo";
$z[$foo] = "bar";
