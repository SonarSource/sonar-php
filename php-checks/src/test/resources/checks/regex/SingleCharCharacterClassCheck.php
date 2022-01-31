<?php

namespace checks\regex;

class SingleCharCharacterClassCheck
{
  function noncompliant($input) {
    preg_match('/[0]/', $input);  // Noncompliant {{Replace this character class by the character itself.}}
    //            ^
    preg_match('/[z]/', $input);  // Noncompliant {{Replace this character class by the character itself.}}
    //            ^
    preg_match('/[ ]/', $input);  // Noncompliant {{Replace this character class by the character itself.}}
    //            ^
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
    preg_match('/[^a]/', $input);
    preg_match('/[]/', $input);
  }
}
