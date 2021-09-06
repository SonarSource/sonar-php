<?php

namespace checks\regex;

class SingleCharacterAlternationCheck
{

  function noncompliant($input)
  {
    preg_match("/a|b|c/", $str); // Noncompliant {{Replace this alternation with a character class.}}
  //             ^^^^^
    preg_match("/a|(b|c)/", $str); // Noncompliant
    preg_match("/abcd|(e|f)gh/", $str); // Noncompliant
    preg_match("/(a|b|c)*/", $str); // Noncompliant
    preg_match("/\\d|x/", $str); // Noncompliant
    preg_match("/\\u1234|\\x{12345}/", $str); // Noncompliant
    preg_match("/😂|😊/", $str); // Noncompliant
    preg_match("/\ud800\udc00|\udbff\udfff/", $str); // Noncompliant
  }

  function compliant($input)
  {
    preg_match("/[abc]/", $str); // Compliant
    preg_match("/[a-c]/", $str); // Compliant
    preg_match("/ab|cd/", $str); // Compliant
    preg_match("/^|$/", $str); // Compliant
    preg_match("/|/", $str); // Compliant
  }

}
