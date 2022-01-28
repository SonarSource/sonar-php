<?php

namespace checks\regex;

class SuperfluousCurlyBraceCheck
{
  function noncompliant($input) {
    preg_match('/(abc){1}/', $input);  // Noncompliant {{Remove this unnecessary quantifier.}}
    //                ^^^
    preg_match('/(abc){1,1}/', $input);  // Noncompliant {{Remove this unnecessary quantifier.}}
    //                ^^^^^
    preg_match('/(abc){0}/', $input);  // Noncompliant {{Remove this unnecessarily quantified expression.}}
    //           ^^^^^^^^
    preg_match('/(abc){0,0}/', $input);  // Noncompliant {{Remove this unnecessarily quantified expression.}}
    //           ^^^^^^^^^^
  }

  function compliant($input) {
    preg_match('/(abc){1,}/', $input);
    preg_match('/(abc){1,2,3}/', $input);

    preg_match('/(abc){0,}/', $input);
    preg_match('/(abc){2}/', $input);
    preg_match('/(abc){1,2}/', $input);
    preg_match('/(abc){0,1}/', $input);
  }
}
