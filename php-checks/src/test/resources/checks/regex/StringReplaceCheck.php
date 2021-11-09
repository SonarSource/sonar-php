<?php

namespace checks\regex;

class StringReplaceCheck
{
  function noncompliant($input)
  {
    $init = "Bob is a Bird... Bob is a Plane... Bob is Superman!";
    preg_replace("/Bob is/", "It's", $init); // Noncompliant {{Replace this "preg_replace()" call by a "str_replace()" function call.}}
  //^^^^^^^^^^^^   ^^^^^^<
    preg_replace("/\.\.\./", ";", $init); // Noncompliant
    preg_replace("/\Q...\E/", ";", $init); // Noncompliant
    preg_replace("/\\\\/", "It's", $init); // Noncompliant
    preg_replace("/\./", "It's", $init); // Noncompliant
    preg_replace("/!/", ".", $init); // Noncompliant
    preg_replace("/\n/", " ", $init); // Noncompliant
    preg_replace("/\"/", " ", $init); // Noncompliant
    preg_replace("/{/", "It's", $init); // Noncompliant
  }

  function compliant($input)
  {
    $init = "Bob is a Bird... Bob is a Plane... Bob is Superman!";
    preg_replace("/(?i)bird/", "bird", $init);
    preg_replace("/\w*\sis/", "It's", $init);
    preg_replace("/\.{3}/", ";", $init);
    preg_replace("/\w/", "It's", $init);
    preg_replace("/\s/", "It's", $init);
    preg_replace($input, "It's", $init);
    preg_replace("/./", "It's", $init);
    preg_replace("/$/", "It's", $init);
    preg_replace("/|/", "It's", $init);
    preg_replace("/(/", "It's", $init);
    preg_replace("/()/", "It's", $init);
    preg_replace("/[/", "It's", $init);
    preg_replace("/[a-z]]/", "It's", $init);
    preg_replace("/x{3}/", "It's", $init);
    preg_replace("/^/", "It's", $init);
    preg_replace("/?/", "It's", $init);
    preg_replace("/x?/", "It's", $init);
    preg_replace("/*/", "It's", $init);
    preg_replace("/x*/", "It's", $init);
    preg_replace("/+/", "It's", $init);
    preg_replace("/x+/", "It's", $init);
    preg_replace("/[\\\\]/", "_", $init);
    preg_replace(PATTERN, "_", $init);
    preg_replace("//u", "_", $init);
    preg_match($input, "/c/", $init);
  }

  function patternAsVariable() {
    $plane = "/c/";
  //           ^> {{Expression without regular expression features.}}
    preg_replace($plane, "UFO", $init); // Noncompliant
  //^^^^^^^^^^^^
  }

}
