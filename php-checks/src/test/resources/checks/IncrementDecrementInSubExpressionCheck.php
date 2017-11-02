<?php

$i++;            // OK
++$i;            // OK
$i--;            // OK
--$i;            // OK

foo[$i]++;       // OK

foo[$i++] = 0;   // Noncompliant {{Extract this increment or decrement operator into a dedicated statement.}}
//  ^^^^
foo[$i--] = 0;   // Noncompliant
foo[++$i] = 0;   // Noncompliant
foo[--$i] = 0;   // Noncompliant

foo[~$i] = 0;    // OK

for ($i = 0; $i < 3; $i++); // OK
for (; $i++; ); // OK
for ($j++; ; ); // OK
for ($i = 0; $i < 3*($i++); $i++); // Noncompliant
for ($i = 0; $i < 3; ($i++)++); // Noncompliant
