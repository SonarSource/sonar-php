<?php

namespace checks\regex;

class InvalidDelimiterCheck
{
  function noncompliant($input) {
    preg_match('/Hello, world!', $input); // Noncompliant {{Add the missing "/" delimiter to this regular expression.}}
    preg_match('Hello, world!/', $input); // Noncompliant {{Add delimiters to this regular expression.}}
    preg_match('.*', $input);             // Noncompliant
    preg_match('/.*', $input);            // Noncompliant
    preg_match('.*#', $input);            // Noncompliant
    preg_match('(.*#', $input);           // Noncompliant
    preg_match('/.*#', $input);           // Noncompliant
    preg_match('\/.*/', $input);          // Noncompliant
    preg_match('     /.*#', $input);      // Noncompliant
    preg_match('w.*w', $input);           // Noncompliant
  }

  function compliant($input) {
    preg_match('/.*/', $input);
    preg_match('/.*/mi', $input);
    preg_match('#.*#', $input);
    preg_match('(.*)', $input);
    preg_match('{.*}', $input);
    preg_match('[.*]', $input);
    preg_match('~.*~', $input);
    preg_match('//', $input);
    preg_match('~', $input);              // to short
    preg_match('_/continue$_', $input);
  }
}
