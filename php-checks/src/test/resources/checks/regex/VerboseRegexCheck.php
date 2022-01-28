<?php

namespace checks\regex;

class VerboseRegexCheck
{
  function noncompliant($input) {
    preg_match('/[\s\S]/s', $input);          // Noncompliant {{Use concise character class syntax '.' instead of '[\s\S]'.}}
    //           ^^^^^^
    preg_match('/[\d\D]/', $input);           // Noncompliant {{Use concise character class syntax '.' instead of '[\d\D]'.}}
    preg_match('/[\w\W]/', $input);           // Noncompliant {{Use concise character class syntax '.' instead of '[\w\W]'.}}
    preg_match('/[0-9]/', $input);            // Noncompliant {{Use concise character class syntax '\d' instead of '[0-9]'.}}
    preg_match('/foo[0-9]barr/', $input);     // Noncompliant
    //              ^^^^^
    preg_match('/[^0-9]/', $input);           // Noncompliant {{Use concise character class syntax '\D' instead of '[^0-9]'.}}
    preg_match('/[A-Za-z0-9_]/', $input);     // Noncompliant {{Use concise character class syntax '\w' instead of '[A-Za-z0-9_]'.}}
    preg_match('/[0-9_A-Za-z]/', $input);     // Noncompliant
    preg_match('/[^A-Za-z0-9_]/', $input);    // Noncompliant {{Use concise character class syntax '\W' instead of '[^A-Za-z0-9_]'.}}
    preg_match('/[^0-9_A-Za-z]/', $input);    // Noncompliant
    preg_match('/x{0,1}/', $input);           // Noncompliant {{Use concise quantifier syntax '?' instead of '{0,1}'.}}
    preg_match('/x{0,1}?/', $input);          // Noncompliant
    preg_match('/x{0,}/', $input);            // Noncompliant {{Use concise quantifier syntax '*' instead of '{0,}'.}}
    preg_match('/x{0,}?/', $input);           // Noncompliant
    preg_match('/x{1,}/', $input);            // Noncompliant {{Use concise quantifier syntax '+' instead of '{1,}'.}}
    preg_match('/x{1,}?/', $input);           // Noncompliant
    preg_match('/x{2,2}/', $input);           // Noncompliant {{Use concise quantifier syntax '{2}' instead of '{2,2}'.}}
    preg_match('/x{2,2}?/', $input);          // Noncompliant
  }

  function compliant($input) {
    preg_match('/[x]/', $input);
    preg_match('/[12]/', $input);
    preg_match('/[1234]/', $input);
    preg_match('/[1-3]/', $input);
    preg_match('/[1-9abc]/', $input);
    preg_match('/[1-9a-bAB]/', $input);
    preg_match('/[1-9a-bA-Z!]/', $input);
    preg_match('/[1-2[a][b][c]]/', $input);
    preg_match('/[0-9[a][b][c]]/', $input);
    preg_match('/[0-9a-z[b][c]]/', $input);
    preg_match('/[0-9a-zA-Z[c]]/', $input);
    preg_match('/x?/', $input);
    preg_match('/x*/', $input);
    preg_match('/x+/', $input);
    preg_match('/x{2}/', $input);
    preg_match('/[\s\S]/', $input);
    preg_match('/[\w\S]/', $input);
    preg_match('/[\d\S]/', $input);
    preg_match('/[\s\d]/', $input);
  }
}
