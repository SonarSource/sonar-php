<?php
$x = $a;
  $x = $x; // NOK {{Remove or correct this useless self-assignment}}
//^^^^^^^
$x->y = $x->z;
$x->y = $x->y; // NOK
$x =& $a;
$x =& $x; // NOK
$x += $x;
$x = $x + $y;
$x = $y = $z;
