<?php

namespace checks\regex;

class SingleCharCharacterClassCheck
{
  function noncompliant($input) {
    preg_match('/[0]/', $input);  // Noncompliant {{Replace this character class by the character itself.}}
    //            ^

    // The following couple of testcases would have been Noncompliant *if* PHP regexes supported nested char classes:
    // preg_match('/[[a]]/', $input);  // Noncompliant {{Replace this character class by the character itself.}}
    // preg_match('/[1-2[3]4-5]/', $input);  // Noncompliant {{Replace this character class by the character itself.}}
  }

  function compliant($input) {
    preg_match('/[\\]/', $input);
    preg_match('/[^]/', $input);
    preg_match('/[$]/', $input);
    preg_match('/[*]/', $input);
    preg_match('/[+]/', $input);
    preg_match('/[?]/', $input);
    preg_match('/[.]/', $input);
    preg_match('/[|]/', $input);
    preg_match('/[(]/', $input);
    preg_match('/[{]/', $input);
    preg_match('/[[]/', $input);
    preg_match('/[0-1]/', $input);
    preg_match('/[^abc]/', $input);
  }
}
