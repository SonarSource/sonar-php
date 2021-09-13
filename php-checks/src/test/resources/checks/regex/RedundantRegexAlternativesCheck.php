<?php

namespace checks\regex;

class RedundantRegexAlternativesCheck
{

  function noncompliant($input)
  {
    preg_match("/[ab]|    a/x", $input); // Noncompliant {{Remove or rework this redundant alternative.}}
    //           ^^^^>    ^
    preg_match("/.|a/", $input); // Noncompliant
    preg_match("/a|./", $input); // Noncompliant
    preg_match("/(.)|(a)/", $input); // Noncompliant
    preg_match("/a|b|bc?/", $input); // Noncompliant
    preg_match("/a|b|bc*/", $input); // Noncompliant
    preg_match("/a|b|bb*/", $input); // Noncompliant
    preg_match("/a|b|a|b|a|b|a|b/", $input); // Noncompliant 2
    preg_match("/[1-2]|[1-4]|[1-8]|[1-3]/", $input); // Noncompliant
    preg_match("/1|[1-2]/", $input); // Noncompliant
  }

  function compliant($input)
  {
    preg_match("/(a)|(.)/", $input); // Compliant
    preg_match("/a|(.)/", $input); // Compliant
    preg_match("/(a)|./", $input); // Compliant
    preg_match("/a|b|bc+/", $input); // Compliant
    preg_match("/|a/", $input); // Compliant
    preg_match("/[ab]/", $input); // Compliant
    preg_match("/.*/", $input); // Compliant
    preg_match("/[[:space:]]|x/", $input); // Compliant
    preg_match("/x|[[:space:]]/", $input); // Compliant
    preg_match("/[[:space:]]|\n/", $input); // Compliant, false negative
  }

}
