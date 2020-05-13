<?php

if (count($a) >= 0) { // Noncompliant
//  ^^^^^^^^^
  echo $a[0];
}

if (count($a_2) >= 0) { // Compliant
  echo $a_2;
}


$b = getData();
echo $b[0];
if (count($b) > 0) { // Noncompliant
  echo "foo";
}

function f(array $d) {
  if (count($d) > 0) {
    echo "foo";
  }
}

if (count($c) >= 0) { // Compliant - We are not sure that $b is an array
  echo "foo";
}
