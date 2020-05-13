<?php

if (count($a) >= 0) { // Noncompliant
//  ^^^^^^^^^
  echo $a[0];
}

if (count($b) >= 0) { // Compliant - We are not sure that $b is an array
  echo "foo";
}
