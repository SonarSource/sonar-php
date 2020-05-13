<?php

if (count($a) >= 0) { // Noncompliant
//  ^^^^^^^^^
  echo $a[0];
}

if (count($a_2) >= 0) { // Compliant
  echo $a_2;
}

if (0 < count($a_3)) { // Noncompliant
  echo $a_3[0];
}

if (count($a_4) >= 1) { // Compliant
  echo $a_4[1];
}

$b = getData();
echo $b[0];
if (count($b) > 0) { // Noncompliant
  echo "foo";
}

if (count($c) >= 0) { // Compliant - We are not sure that $b is an array
  echo "foo";
}

if (foo($c_1) > 0) { // Compliant
  echo $c_1[0];
}

if (foo($c_2)) {
  echo $c_2[0];
}

function f(array $d) {
  if (count($d) > 0) { // Noncompliant
    echo "foo";
  }
}

function f_2(string $d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

function f_3(SomeObject $d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

if (count($x)) { // Compliant - FN
  echo $x[0];
}

if (count()) { // Compliant
  echo $y[0];
}

if (count(z) + 1) { // Compliant
  echo $y[0];
}
