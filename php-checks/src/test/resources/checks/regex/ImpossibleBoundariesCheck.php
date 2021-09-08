<?php

namespace checks\regex;

class ImpossibleBoundariesCheck
{
  function noncompliant($input)
  {
    preg_match('/$[a-z]^/', $input); // Noncompliant 2
    preg_match('/$[a-z]/', $input); // Noncompliant
    preg_match('/$(abc)/', $input); // Noncompliant
    preg_match('/[a-z]^/', $input); // Noncompliant
    preg_match('/\\Z[a-z]/', $input); // Noncompliant
    preg_match('/\\z[a-z]/', $input); // Noncompliant
    preg_match('/[a-z]\\A/', $input); // Noncompliant
    preg_match('/($)a/', $input); // Noncompliant
    preg_match('/a$|$a/', $input); // Noncompliant
    preg_match('/^a|a^/', $input); // Noncompliant
    preg_match('/a(b|^)/', $input); // Noncompliant
    preg_match('/(?=abc^)/', $input); // Noncompliant
    preg_match('/(?!abc^)/', $input); // Noncompliant
    preg_match('/abc(?=^abc)/', $input); // Noncompliant
    preg_match('/abc(?<=$abc)/', $input); // Noncompliant
    preg_match('/abc(?<=abc$)def/', $input); // Noncompliant
    preg_match('/(?:abc(X|^))*Y?/', $input); // Noncompliant
  }

  function probablyNonCompliant($input) {
    preg_match('/$.*/', $input); // Noncompliant {{Remove or replace this boundary that can only match if the previous part matched the empty string because it appears before mandatory input.}}
    preg_match('/$.?/', $input); // Noncompliant 

    preg_match('/$a*/', $input); // Noncompliant 
    preg_match('/$a?/', $input); // Noncompliant 
    preg_match('/$[abc]*/', $input); // Noncompliant 
    preg_match('/$[abc]?/', $input); // Noncompliant 

    preg_match('/.*^/', $input); // Noncompliant {{Remove or replace this boundary that can only match if the previous part matched the empty string because it appears after mandatory input.}}
    preg_match('/.?^/', $input); // Noncompliant 

    preg_match('/a*^/', $input); // Noncompliant 
    preg_match('/a?^/', $input); // Noncompliant 
    preg_match('/[abc]*^/', $input); // Noncompliant 
    preg_match('/[abc]?^/', $input); // Noncompliant 

    preg_match('/$.*^/', $input); // Noncompliant 2
    preg_match('/$.?^/', $input); // Noncompliant 2
    preg_match('/$a*^/', $input); // Noncompliant 2
    preg_match('/$a?^/', $input); // Noncompliant 2
    preg_match('/$[abc]*^/', $input); // Noncompliant 2
    preg_match('/$[abc]?^/', $input); // Noncompliant 2
  }

  function compliant($input)
  {
    preg_match('/^[a-z]$/', $input);
    preg_match('/^$/', $input);
    preg_match('/^(?i)$/', $input);
    preg_match('/^$(?i)/', $input);
    preg_match('/^abc$|^def$/', $input);
    preg_match('/(?i)^abc$/', $input);
    preg_match('/()^abc$/', $input);
    preg_match('/^abc$()/', $input);
    preg_match('/^abc$\\b/', $input);
    preg_match('/(?=abc)^abc$/', $input);
    preg_match('/(?=^abc$)abc/', $input);
    preg_match('/(?!^abc$)abc/', $input);
    preg_match('/abc(?<=^abc$)/', $input);
    preg_match('/^\\d$(?<!3)/', $input);
    preg_match('/(?=$)/', $input);
    preg_match("/(?i)(true)(?=(?:[^']|'[^']*')*$)/", $input);
    preg_match('/(?:abc(X|$))*Y?/', $input);
    preg_match('/(?:x*(Xab|^)abc)*Y?/', $input);
  }
}
