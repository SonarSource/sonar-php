<?php

$i++;            // OK
++$i;            // OK
$i--;            // OK
--$i;            // OK

foo[$i]++;       // OK

foo[$i++] = 0;   // NOK
foo[$i--] = 0;   // NOK
foo[++$i] = 0;   // NOK
foo[--$i] = 0;   // NOK

foo[~$i] = 0;    // OK

for ($i = 0; $i < 3; $i++); // OK
