<?php
$x = $a == $b;
$x = $a == $a; // NOK [[secondary=+0]] {{Identical sub-expressions on both sides of operator "=="}}
//         ^^
$x = $a != $a; // NOK {{Identical sub-expressions on both sides of operator "!="}}
$x = $a == $b && $a == $b; // NOK
$x = $a == $b || $a == $b; // NOK
$x = $a == $b or $a == $b; // NOK
$x = $a == $b and $a == $b; // NOK
$x = $a > $a; // NOK
$x = $a <= $a; // NOK
$x = 5 / 5; // NOK
$x = 5 - 5; // NOK
$x = $x + $x;
$x = $x * $x;
$x = $x . $x;
$x = $x;
$x = $a == $a + $b;
$x = $a << $a; // NOK
$x = 1 + $a << 1 + $a; // NOK
$x = 1 << 1;
$x = $a / 1000 / 1000;
$x = $a + 1 == $a + $b;
$x = $a + 1 == $a + 2 + 1;
