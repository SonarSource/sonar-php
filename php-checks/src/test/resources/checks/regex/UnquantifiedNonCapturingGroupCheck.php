<?php

namespace checks\regex;

class UnquantifiedNonCapturingGroupCheck
{
  function noncompliant($input) {
    preg_match('/(?:number)/', $input);                // Noncompliant {{Unwrap this unnecessarily grouped subpattern.}}
    //           ^^^^^^^^^^
    preg_match('/(?:number)\d{2}/', $input);           // Noncompliant
    preg_match('/(?:number(?:two){2})/', $input);      // Noncompliant
    //           ^^^^^^^^^^^^^^^^^^^^
    preg_match('/(?:number(?:two)){2}/', $input);      // Noncompliant
    //                    ^^^^^^^
    preg_match('/foo(?:number)bar/', $input);          // Noncompliant
    preg_match('/(?:)/', $input);                      // Noncompliant
  }

  function compliant($input) {
    preg_match('/(?:number)?+/', $input);
    preg_match('/number\d{2}/', $input);
    preg_match('/(?:number)?\d{2}/', $input);
    preg_match('/(?:number|string)/', $input);
  }
}
