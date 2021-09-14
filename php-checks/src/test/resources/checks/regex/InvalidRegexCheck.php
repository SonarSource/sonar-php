<?php

namespace checks\regex;

class InvalidRegexCheck
{
  function noncompliant($input)
  {
    preg_match('/(/', $input); // Noncompliant {{Fix the syntax error inside this regex.}}
    //            ^
    //            ^@-1< {{Expected ')', but found the end of the regex}}
    preg_match('/x{1,2,3}|(/', $input); // Noncompliant {{Fix the syntax errors inside this regex.}}
    //                ^
    //                ^@-1< {{Expected '}', but found ','}}
    //                     ^@-2< {{Expected ')', but found the end of the regex}}
    preg_match('/$[a-z^/', $input); // Noncompliant {{Fix the syntax error inside this regex.}}
    //                 ^
    //                 ^@-1< {{Expected ']', but found the end of the regex}}
    preg_match("/(\\w+-(\\d+)/", $input); // Noncompliant {{Fix the syntax error inside this regex.}}
    //                       ^
    //                       ^@-1< {{Expected ')', but found the end of the regex}}
  }

  function falsePositives($input) {
    // backreference syntax we do not support yet - SONARPHP-1241
    preg_match("/(?<name>(?:foo|bar))\\k'name'/", $input); // Noncompliant
    preg_match("/(?<name>(?:foo|bar))\\k{name}/", $input); // Noncompliant
    preg_match("/(?<name>(?:foo|bar))(?P=name)/", $input); // Noncompliant
    preg_match("/(?<name>(?:foo|bar))\\g{name}/", $input); // Noncompliant

    // named subpattern syntax we do not support yet - SONARPHP-1240
    preg_match("/(?'name'abc)/", $input); // Noncompliant
    preg_match("/(?P<name>abc)/", $input); // Noncompliant

    // duplicate subpattern numbers with (?|..) - SONARPHP-1242
    // both matching groups get the number 1. Matches foofoo and barbar
    preg_match("/(?|(foo)|(bar))\\1/", $input); // Noncompliant
  }

  function compliant($input)
  {
    preg_match('/$[a-z]^/', $input);
  }
}
