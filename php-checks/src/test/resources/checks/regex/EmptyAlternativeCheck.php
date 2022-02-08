<?php

namespace checks\regex;

class EmptyAlternativeCheck
{
  function noncompliant($input) {
    preg_match("/(mandatory||optional)/", $input); // Noncompliant {{Remove this empty alternative.}}
    //                      ^

    preg_match("/|mandatory|-optional/", $input); // Noncompliant
    //           ^

    preg_match("/mandatory|-optional|/", $input); // Noncompliant
    //                              ^

    // Noncompliant@+3
    preg_match("/(
                \'[^\']*(\'|$)| # - a string that starts with a quote, up until the next quote or the end of the string
                |               # or
                \S              # - a non-whitespace character
                )/x", $input);

    preg_match("/|mandatory|-optional/", $input);              // Noncompliant
    preg_match("/(mandatory|(|O|o|)ptional|)/", $input);       // Noncompliant
    preg_match("/(|mandatory|optional)?/", $input);            // Noncompliant
    //            ^
    preg_match("/mandatory(-optional|){2}/", $input);          // Noncompliant
    //                              ^
  }

  function compliant($input) {
    preg_match("/(mandatory|optional|)/", $input);
    preg_match("/mandatory(-optional|)/", $input);
    preg_match("/mandatory(|-optional)/", $input);
    preg_match("/mandatory(|-optional)/", $input);
    preg_match("/mandatory(-optional|)/", $input);
    preg_match("/(mandatory(|-optional))?/", $input);
  }
}
