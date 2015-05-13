<?php
$x = $a;
$x = $x; // Noncompliant
$x->y = $x->z;
$x->y = $x->y; // Noncompliant
$x =& $a;
$x =& $x; // Noncompliant
$x += $x;
$x = $x + $y;
$x = $y = $z;
