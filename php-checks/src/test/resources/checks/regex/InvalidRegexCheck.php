<?php

namespace checks\regex;

class InvalidRegexCheck
{
  function noncompliant($input)
  {
    preg_match('/(/', $input); // Noncompliant {{Fix the syntax error inside this regex.}}
    //            ^
    //            ^@-1< {{Expected ')', but found the end of the regex}}
    preg_match('/x{1,2,3}|(/', $input); // Noncompliant {{Fix the syntax error inside this regex.}}
    //                     ^
    //                     ^@-1< {{Expected ')', but found the end of the regex}}
    preg_match('/$[a-z^/', $input); // Noncompliant {{Fix the syntax error inside this regex.}}
    //                 ^
    //                 ^@-1< {{Expected ']', but found the end of the regex}}
    preg_match("/(\\w+-(\\d+)/", $input); // Noncompliant {{Fix the syntax error inside this regex.}}
    //                       ^
    //                       ^@-1< {{Expected ')', but found the end of the regex}}
  }

  function falsePositives($input) {
    // duplicate subpattern numbers with (?|..) - SONARPHP-1242
    // both matching groups get the number 1. Matches foofoo and barbar
    preg_match("/(?|(foo)|(bar))\\1/", $input); // Noncompliant
  }

  function compliant($input)
  {
    preg_match('/$[a-z]^/', $input);
    preg_replace('/[^A-Za-z0-9\-\][\^!@#$%&*)(+=}{]/', '', $input);

    preg_match("/((?:[\\0\\r\\n]|\u{202A}|\u{202B}|\u{202D}|\u{202E}|\u{2066}|\u{2067}|\u{2068}|\u{202C}|\u{2069})++)(.)/", $input);
  }
}
