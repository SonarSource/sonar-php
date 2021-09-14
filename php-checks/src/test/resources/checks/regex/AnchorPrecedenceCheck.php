<?php

namespace checks\regex;

class DuplicatesInCharacterClassCheck
{

  function noncompliant($input)
  {
    preg_match("/^a|b|c$/", $input); // Noncompliant {{Group parts of the regex together to make the intended operator precedence explicit.}}
    preg_match("/^a|b|cd/", $input); // Noncompliant
    preg_match("/(?i)^a|b|cd/", $input); // Noncompliant
    preg_match("/(?i:^a|b|cd)/", $input); // Noncompliant
    preg_match("/a|b|c$/", $input); // Noncompliant
    preg_match("/\\Aa|b|c\\Z/", $input); // Noncompliant
    preg_match("/\\Aa|b|c\\z/", $input); // Noncompliant
  }

  function compliant($input)
  {
    preg_match("/^(?:a|b|c)$/", $input);
    preg_match("/(?:^a)|b|(?:c$)/", $input);
    preg_match("/^abc$/", $input);
    preg_match("/a|b|c/", $input);
    preg_match("/^a$|^b$|^c$/", $input);
    preg_match("/^a$|b|c/", $input);
    preg_match("/a|b|^c$/", $input);
    preg_match("/^a|^b$|c$/", $input);
    preg_match("/^a|^b|c$/", $input);
    preg_match("/^a|b$|c$/", $input);
    // Only beginning and end of line/input boundaries are considered - not word boundaries
    preg_match("/\\ba|b|c\\b/", $input);
    preg_match("/\\ba\\b|\\bb\\b|\\bc\\b/", $input);
    // If multiple alternatives are anchored, but not all, that's more likely to be intentional than if only the first
    // one were anchored, so we won't report an issue for the following line:
    preg_match("/^a|^b|c/", $input);
    preg_match("/aa|bb|cc/", $input);
    preg_match("/^/", $input);
    preg_match("/^[abc]$/", $input);
    preg_match("/|/", $input);
    preg_match("/[/", $input);
    preg_match("/(?i:^)a|b|c/", $input); // False negative; we don't find the anchor if it's hidden inside a sub-expression
  }

}
