<?php

for ($a = 0; $a < 42; $a++) {
  $a = 0;                                // Noncompliant {{Refactor the code to avoid updating the loop counter "$a" within the loop body.}}
//^^
}

for ($a = 0; $a < 42; $a++):
  $a = 0;                                // Noncompliant
endfor;

for ($d = 0, $e = 0; $d < 42; $d++) {
  $d = 0;                                // Noncompliant
  $e = 0;                                // Noncompliant
}

$g;
for ($f = 0; $f < 42; $f++) {
  $f = 0;                                // Noncompliant
  $g = 0;                                // OK
  for ($g = 0; $g < 42; $g++) {
    $g = 0;                              // Noncompliant
    $f = 0;                              // Noncompliant
  }
  $f = 0;                                // Noncompliant
  $g = 0;                                // OK
}

$g = 0;                                  // OK

for ($h = 0; $h < 42; $h++) {
  $h =                                   // Noncompliant
      $h =                               // Noncompliant
          0;
}

$g++;                                    // OK
++$g;                                    // OK
$g = 0;                                  // OK
doSomething($i);                         // OK

for ($i = 0; 0 < 42; $i++) {
  $i++;                                  // Noncompliant
  ++$i;                                  // Noncompliant
  --$i;                                  // Noncompliant
  $i--;                                  // Noncompliant
}

for ($j = 0; $j < 42; $j++) {            // OK
  for ($k = 0; $j++ < 42; $k++) {        // Noncompliant
  }
}

for ($i = 0; $i < 42; $i++) {
  (int)$i;                                // OK
}

for ($i = 0; $i < 10; $i++) {
  for ($k = 0; $k < 20; $i++) {           // Noncompliant
    echo "Hello";
    doSomething($i = 0);                  // Noncompliant
  }
}


for ($a->i++; $a->i < 3; $a->i++) {
  $a->i = 1;                              // Noncompliant {{Refactor the code to avoid updating the loop counter "$a->i" within the loop body.}}
}

for ($a->i++; $a->i < 3; $a->i++) {
  $a->z = 1;                              // OK
}

$i = 0;
for ( ; $i > 0; $i++) {
 $i = 1;                                  // OK
}

for (++$i ; $i > 0; $i++) {
 $i = 1;                                  // Noncompliant
}

foreach ($myArray as $i) {
  $i = 0;                                 // OK - not in scope
}

for ($a = 0; $a < 42; $a++) {
  $a =& $b;                                // Noncompliant
}
