<?php

namespace checks\regex;

class RegexLookaheadCheck
{
  function noncompliant($input)
  {
    preg_match("/(?=a)b/", $input); // Noncompliant {{Remove or fix this lookahead assertion that can never be true.}}
    preg_match("/(?=ac)ab/", $input); // Noncompliant
    preg_match("/(?=a)bc/", $input); // Noncompliant
    preg_match("/(?!a)a/", $input); // Noncompliant
    preg_match("/(?!ab)ab/", $input); // Noncompliant
    preg_match("/(?=a)[^ba]/", $input); // Noncompliant
    preg_match("/(?!.)ab/", $input); // Noncompliant
  }

  function compliant($input)
  {
    preg_match("/(?=a)a/", $input);
    preg_match("/(?=a)../", $input);
    preg_match("/(?=a)ab/", $input);
    preg_match("/(?!ab)../", $input);
    preg_match("/(?<=a)b/", $input);
    preg_match("/a(?=b)/", $input);
    preg_match("/(?=abc)ab/", $input);
  }
}
