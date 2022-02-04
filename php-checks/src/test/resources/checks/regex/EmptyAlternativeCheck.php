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
  }

  function compliant($input) {
    preg_match("/(mandatory|optional|)/", $input);
    preg_match("/mandatory(-optional|)/", $input);
    preg_match("/mandatory(|-optional)/", $input);
  }
}
