<?php
if (count($arr) >= 0) {  }   // Noncompliant {{The count of an array or Countable is always ">=0", so update this test to either "==0" or ">0".}}
//  ^^^^^^^^^^^^^^^^
if (cOuNt($arr) < 0) {  }    // Noncompliant
$result = count($arr) >= 0;  // Noncompliant
if (0 > ((((((count($arr)))))))) {  }  // Noncompliant
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
if (0 <= count($arr)) {   } // Noncompliant

if (0 >= count($arr)) { } // Compliant
if (count($arr) != 0) { } // Compliant
if (count($arr) > 0) {  } // Compliant
if (count($arr) >= 1) { } // Compliant
if (foo($arr) >= 0) {   } // Compliant

$arrCount = count($arr);
if ($arrCount >= 0) {   } // Compliant - FN - not following variables
