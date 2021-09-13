<?php

namespace checks\regex;

class PossessiveQuantifierContinuationCheck
{
  public function noncompliant($input)
  {
    preg_match("/a++abc/", $input); // Noncompliant  {{Change this impossible to match sub-pattern that conflicts with the previous possessive quantifier.}}
    preg_match("/\d*+[02468]/", $input); // Noncompliant
    preg_match("/(\d)*+([02468])/", $input); // Noncompliant
    preg_match("/\d++(?:[eE][+-]?\d++)?[fFdD]?/", $input);
    preg_match("/(xx++)+x/", $input); // Noncompliant
    preg_match("/(?:xx++)+x/", $input); // Noncompliant
    preg_match("/(xx++)x/", $input); // Noncompliant
    preg_match("/.*+\w/", $input); // Noncompliant
    preg_match("/.*+\w+/", $input); // Noncompliant
    preg_match("/(a|b|c)*+(a|b)/", $input); // Noncompliant
  }

  public function compliant($input)
  {
    preg_match("/a+abc/", $input);
    preg_match("/a+?abc/", $input);
    preg_match("/a*+\s/", $input);
    preg_match("/aa++bc/", $input);
    preg_match("/\d*+(?<=[02468])/", $input);
    preg_match("/(:[0-9])?+(:[0-9])?+/", $input);
    preg_match("/(bx++)+x/", $input); // FN because limitation of the algorithm when there's infinite loop
    preg_match("/(?(1)(.*)|())/",$input);
  }
}
