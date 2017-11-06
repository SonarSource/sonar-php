<?php

  $length = strlen($name);

  strlen($name); // Noncompliant {{The return value of "strlen" must be used.}}
//^^^^^^

  (count($arr)); // Noncompliant

  $method($arr);

  while($x) {
    count($arr); // Noncompliant
  }

  switch ($x) {
    case 2:
      count($arr); // Noncompliant
      break;
  }

  for (count($arr);    // Noncompliant
       count($arr);
       count($arr)) {   // Noncompliant
  }

  MyClass::strlen($name);

  $length = strlen($name);

  array(1, 2); // Noncompliant
  $arr = array(1, 2);

