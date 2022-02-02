<?php

namespace checks\regex;

class EmptyGroupCheck
{

  function noncompliant($input)
  {
    preg_match("/foo()bar/", $input);    // Noncompliant {{Remove this empty group.}}
    //              ^^
    preg_match("/foo(?:)bar/", $input);  // Noncompliant
    //              ^^^^
    preg_match("/foo(?>)bar/", $input);  // Noncompliant
    //              ^^^^
    preg_match("/foo(?=)bar/", $input);  // Noncompliant
    //              ^^^^
    preg_match("/foo(?!)bar/", $input);  // Noncompliant
    //              ^^^^
    preg_match("/foo(?<=)bar/", $input); // Noncompliant
    //              ^^^^^
    preg_match("/foo(?<!)bar/", $input); // Noncompliant
    //              ^^^^^

    preg_match("/(foo()bar)/", $input);    // Noncompliant
    //               ^^
    preg_match("/(foo(?:)bar)/", $input);  // Noncompliant
    //               ^^^^
    preg_match("/(foo(?>)bar)/", $input);  // Noncompliant
    //               ^^^^
    preg_match("/(foo(?=)bar)/", $input);  // Noncompliant
    //               ^^^^
    preg_match("/(foo(?!)bar)/", $input);  // Noncompliant
    //               ^^^^
    preg_match("/(foo(?<=)bar)/", $input); // Noncompliant
    //               ^^^^^
    preg_match("/(foo(?<!)bar)/", $input); // Noncompliant
    //               ^^^^^
  }

  function compliant($input)
  {
    preg_match("/foo(?-)bar/", $input);   // Compliant
    preg_match("/foo(?-x)bar/", $input);  // Compliant
    preg_match("/(foo(?-)bar)/", $input); // Compliant

    preg_match("/foo(x)bar/", $input);   // Compliant
    preg_match("/foo(?:x)bar/", $input); // Compliant
    preg_match("/foo(?>x)bar/", $input); // Compliant
    preg_match("/foo(?=x)bar/", $input); // Compliant
    preg_match("/foo(?!x)bar/", $input); // Compliant
    preg_match("/foo(?<=x)bar/", $input); // Compliant
    preg_match("/foo(?<!x)bar/", $input); // Compliant

    preg_match("/[foo()bar]/", $input);   // Compliant
    preg_match("/[foo(?-)bar]/", $input); // Compliant
    preg_match("/[foo(?:)bar]/", $input); // Compliant
    preg_match("/[foo(?>)bar]/", $input); // Compliant
    preg_match("/[foo(?=x)bar]/", $input); // Compliant
    preg_match("/[foo(?!x)bar]/", $input); // Compliant
    preg_match("/[foo(?<=x)bar]/", $input); // Compliant
    preg_match("/[foo(?<!x)bar]/", $input); // Compliant

    preg_match("/(foo(|)bar)/", $input);   // Compliant
    preg_match("/(foo(?-|)bar)/", $input); // Compliant
    preg_match("/(foo(?:|)bar)/", $input); // Compliant
    preg_match("/(foo(?>|)bar)/", $input); // Compliant
    preg_match("/(foo(?=|)bar)/", $input); // Compliant
    preg_match("/(foo(?!|)bar)/", $input); // Compliant
    preg_match("/(foo(?<=|)bar)/", $input); // Compliant
    preg_match("/(foo(?<!|)bar)/", $input); // Compliant
  }
}
