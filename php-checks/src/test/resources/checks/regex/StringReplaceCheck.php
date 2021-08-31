<?php

class StringReplaceCheck
{
  function noncompliant($input) {
    $init = "Bob is a Bird... Bob is a Plane... Bob is Superman!";
    $plane = "/c/";
    preg_replace("/Bob is/", "It's", $init); // Noncompliant {{Replace this "preg_replace()" call by a "str_replace()" function call.}}
  //^^^^^^^^^^^^
    preg_replace($plane, "UFO", $init); // Noncompliant
    preg_replace("/\.\.\./", ";", $init); // Noncompliant
    preg_replace("/\Q...\E/", ";", $init); // Noncompliant
    preg_replace("/\\\\/", "It's", $init); // Noncompliant
    preg_replace("/\./", "It's", $init); // Noncompliant
    preg_replace("/!/", ".", $init); // Noncompliant
    preg_replace("/\n/", " ", $init); // Noncompliant
  }

  function compliant($input) {
    $init = "Bob is a Bird... Bob is a Plane... Bob is Superman!";
    preg_replace("/(?i)bird/", "bird", $init); // Compliant
    preg_replace("/\w*\sis/", "It's", $init); // Compliant
    preg_replace("/\.{3}/", ";", $init); // Compliant
    preg_replace("/\w/", "It's", $init); // Compliant
    preg_replace("/\s/", "It's", $init); // Compliant
    preg_replace($input, "It's", $init); // Compliant
    preg_replace("/./", "It's", $init); // Compliant
    preg_replace("/$/", "It's", $init); // Compliant
    preg_replace("/|/", "It's", $init); // Compliant
    preg_replace("/(/", "It's", $init); // Compliant
    preg_replace("/()/", "It's", $init); // Compliant
    preg_replace("/[/", "It's", $init); // Compliant
    preg_replace("/[a-z]]/", "It's", $init); // Compliant
    preg_replace("/{/", "It's", $init); // Compliant
    preg_replace("/x{3}/", "It's", $init); // Compliant
    preg_replace("/^/", "It's", $init); // Compliant
    preg_replace("/?/", "It's", $init); // Compliant
    preg_replace("/x?/", "It's", $init); // Compliant
    preg_replace("/*/", "It's", $init); // Compliant
    preg_replace("/x*/", "It's", $init); // Compliant
    preg_replace("/+/", "It's", $init); // Compliant
    preg_replace("/x+/", "It's", $init); // Compliant
    preg_replace("/[\\\\]/", "_", $init); // Compliant
    preg_match($input, "/c/", $init); // Compliant
  }

}
