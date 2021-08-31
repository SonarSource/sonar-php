<?php

  preg_replace("/c/", "a", $input); // Noncompliant {{Replace this "preg_replace()" call by a "str_replace()" function call.}}
//^^^^^^^^^^^^
  preg_replace("/casdasd/", "a", $input); // Noncompliant
  preg_replace("+c+", "a", $input); // Noncompliant
  preg_replace("/\d/", "a", $input); // Compliant
  preg_replace("/c\d/", "a", $input); // Compliant
  preg_replace("/[c]/", "a", $input); // Compliant
  preg_replace("/\"/", "'a'", $input); // Noncompliant

  preg_match("/c/", $input); // Compliant

  $knownPattern = "/c/";
  preg_replace($knownPattern, "a", $input); // Noncompliant
  preg_replace($unknownPattern, "a", $input); // Compliant

  preg_replace("/+/", "a", $input); // Compliant
