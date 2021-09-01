<?php

namespace checks\regex;

class EmptyStringRepetitionCheck
{

  function noncompliant($input)
  {
    preg_match("/(?:)*/", $input); // Noncompliant {{Rework this part of the regex to not match the empty string.}}
    //           ^^^^
    preg_match("/(?:)?/", $input); // Noncompliant
    preg_match("/(?:)+/", $input); // Noncompliant
    preg_match("/()*/", $input); // Noncompliant
    preg_match("/()?/", $input); // Noncompliant
    preg_match("/()+/", $input); // Noncompliant
    preg_match("/xyz|(?:)*/", $input); // Noncompliant
    preg_match("/(?:|x)*/", $input); // Noncompliant
    preg_match("/(?:x|)*/", $input); // Noncompliant
    preg_match("/(?:x|y*)*/", $input); // Noncompliant
    preg_match("/(?:x*|y*)*/", $input); // Noncompliant
    preg_match("/(?:x?|y*)*/", $input); // Noncompliant
    preg_match("/(?:x*)*/", $input); // Noncompliant
    preg_match("/(?:x?)*/", $input); // Noncompliant
    preg_match("/(?:x*)?/", $input); // Noncompliant
    preg_match("/(?:x?)?/", $input); // Noncompliant
    preg_match("/(?:x*)+/", $input); // Noncompliant
    preg_match("/(?:x?)+/", $input); // Noncompliant
    preg_match("/(x*)*/", $input); // Noncompliant
    preg_match("/((x*))*/", $input); // Noncompliant
    preg_match("/(?:x*y*)*/", $input); // Noncompliant
    preg_match("/(?:())*/", $input); // Noncompliant
    preg_match("/(?:(?:))*/", $input); // Noncompliant
    preg_match("/((?i))*/", $input); // Noncompliant
    preg_match("/(())*/", $input); // Noncompliant
    preg_match("/(()x*)*/", $input); // Noncompliant
    preg_match("/(()|x)*/", $input); // Noncompliant
    preg_match("/($)*/", $input); // Noncompliant
    preg_match("/(\b)*/", $input); // Noncompliant
    preg_match("/((?!x))*/", $input); // Noncompliant
  }

  function compliant($input)
  {
    preg_match("/x*|/", $input);
    preg_match("/x*|/", $input);
    preg_match("/x*/", $input);
    preg_match("/x?/", $input);
    preg_match("/(?:x|y)*/", $input);
    preg_match("/(?:x+)+/", $input);
    preg_match("/(?:x+)*/", $input);
    preg_match("/(?:x+)?/", $input);
    preg_match("/((x+))*/", $input);
  }

  function no_duplications($input)
  {
    $regex = "/(?:)*/"; // Noncompliant
    //         ^^^^
    preg_match($regex, $input);
    preg_match($regex, $input);
  }

}
