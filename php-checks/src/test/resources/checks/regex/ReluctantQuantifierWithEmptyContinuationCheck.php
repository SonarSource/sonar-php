<?php

namespace checks\regex;

class ReluctantQuantifierWithEmptyContinuationCheck
{

  function noncompliant($input)
  {
    preg_match("/.*?x?/", $input); // Noncompliant {{Fix this reluctant quantifier that will only ever match 0 repetitions.}}
    //           ^^^
    preg_match("/.+?x?/", $input); // Noncompliant {{Fix this reluctant quantifier that will only ever match 1 repetition.}}
    preg_match("/.{2,4}?x?/", $input); // Noncompliant {{Fix this reluctant quantifier that will only ever match 2 repetitions.}}
    preg_match("/.*?$/", $input); // Noncompliant {{Remove the '?' from this unnecessarily reluctant quantifier.}}
    preg_match("/.*?()$/", $input); // Noncompliant {{Remove the '?' from this unnecessarily reluctant quantifier.}}
  }

  function compliant($input)
  {
    preg_match("/.*?x/", $input);
    preg_match("/.*?x$/", $input);
    preg_match("/.*?[abc]/", $input);
  }

}
