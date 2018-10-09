<?php
$x = $a == $b;
$x = $a == $a; // Noncompliant {{Identical sub-expressions on both sides of operator "=="}}
//   ^^>   ^^
$x = $a != $a; // Noncompliant {{Identical sub-expressions on both sides of operator "!="}}
$x = $a == $b && $a == $b; // Noncompliant
$x = $a == $b || $a == $b; // Noncompliant
$x = $a == $b or $x = $a == $b; // Noncompliant
$x = $a == $b and $x = $a == $b; // Noncompliant
$x = $a > $a; // Noncompliant
$x = $a <= $a; // Noncompliant
$x = 5 / 5; // Noncompliant
$x = 5 - 5; // Noncompliant
$x = $x + $x;
$x = $x * $x;
$x = $x . $x;
$x = $x;
$x = $a == $a + $b;
$x = $a << $a; // Noncompliant
$x = 1 + $a << 1 + $a; // Noncompliant
$x = 1 << 1;
$x = $a / 1000 / 1000;
$x = $a + 1 == $a + $b;
$x = $a + 1 == $a + 2 + 1;
$a = $b or $b; // Compliant because it's parsed as ($a = $b) or $b
