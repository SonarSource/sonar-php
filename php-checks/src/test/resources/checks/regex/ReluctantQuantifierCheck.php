<?php

namespace checks\regex;

class ReluctantQuantifierCheck
{
  function noncompliant($input)
  {
    preg_match("/<.+?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>]++".}}
    preg_match("/<\S+?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>\s]++".}}
    preg_match("/<\\S+?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>\s]++".}}
    preg_match("/<\D+?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>\d]++".}}
    preg_match("/<\W+?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>\w]++".}}

    preg_match("/<.{2,5}?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>]{2,5}+".}}
    preg_match("/<\S{2,5}?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>\s]{2,5}+".}}
    preg_match("/<\D{2,5}?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>\d]{2,5}+".}}
    preg_match("/<\W{2,5}?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>\w]{2,5}+".}}

    preg_match("/<.{2,}?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>]{2,}+".}}
    preg_match("/\".*?\"/"); // Noncompliant {{Replace this use of a reluctant quantifier with "[^\"]*+".}}
    preg_match("/.*?\w/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "\W*+".}}
    preg_match("/.*?\W/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "\w*+".}}
    preg_match("/.*?\p{L}/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "\P{L}*+".}}
    preg_match("/.*?\P{L}/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "\p{L}*+".}}
    preg_match("/\[.*?\]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^\]]*+".}}
    preg_match("/.+?[abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^abc]++".}}
    preg_match("/(?-U:\s)*?\S/", $input);
    preg_match("/(?U:\s)*?\S/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[\s\S]*+".}}
    preg_match("/(?U:a|\s)*?\S/", $input);
    preg_match("/\S*?\s/", $input);
    preg_match("/\S*?(?-U:\s)/", $input);
    preg_match("/\S*?(?U:\s)/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[\S\s]*+".}}
    preg_match("/\S*?(?U)\s/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[\S\s]*+".}}

    // coverage
    preg_match("/(?:(?m))*?a/", $input);
    preg_match("/(?:(?m:.))*?(?:(?m))/", $input);

    // This replacement might not be equivalent in case of full match, but is equivalent in case of split
    preg_match("/.+?[^abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[abc]++".}}

    preg_match("/.+?\x{1F4A9}/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^\x{1F4A9}]++".}}
    preg_match("/<abc.*?>/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>]*+".}}
    preg_match("/<.+?>|otherstuff/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>]++".}}
    preg_match("/(<.+?>)*/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^>]++".}}

    preg_match("/\S+?[abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^abc\s]++".}}
    preg_match("/\D+?[abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^abc\d]++".}}
    preg_match("/\w+?[abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^abc\W]++".}}

    preg_match("/\S*?[abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^abc\s]*+".}}
    preg_match("/\D*?[abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^abc\d]*+".}}
    preg_match("/\w*?[abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[^abc\W]*+".}}

    preg_match("/\S+?[^abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[abc\S]++".}}
    preg_match("/\s+?[^abc]/", $input); // Noncompliant {{Replace this use of a reluctant quantifier with "[abc\s]++".}}
  }

  function compliant($input)
  {
    preg_match("/<[^>]++>/", $input);
    preg_match("/<[^>]+>/", $input);
    preg_match("/<[^>]+?>/", $input);
    preg_match("/<.{42}?>/", $input); // Adding a ? to a fixed quantifier is pointless, but also doesn't cause any backtracking issues
    preg_match("/<.+>/", $input);
    preg_match("/<.++>/", $input);
    preg_match("/<--.?-->/", $input);
    preg_match("/<--.+?-->/", $input);
    preg_match("/<--.*?-->/", $input);
    preg_match("//\*.?\*//", $input);
    preg_match("/<[^>]+>?/", $input);
    preg_match("//", $input);
    preg_match("/.*?(?:a|b|c)/", $input); // Alternatives are currently not covered even if they contain only single characters
  }

  function no_intersection($input) {
    preg_match("/<\d+?>/", $input);
    preg_match("/<\s+?>/", $input);
    preg_match("/<\w+?>/", $input);

    preg_match("/<\s{2,5}?>/", $input);
    preg_match("/<\d{2,5}?>/", $input);
    preg_match("/<\w{2,5}?>/", $input);

    preg_match("/\d+?[abc]/", $input);
    preg_match("/\s+?[abc]/", $input);
    preg_match("/\W+?[abc]/", $input);

    preg_match("/\W*?[abc]/", $input);
    preg_match("/\s*?[abc]/", $input);
    preg_match("/\d*?[abc]/", $input);

    preg_match("/\d*?\p{L}/", $input);
    preg_match("/\d*?\P{L}/", $input); // There is an intersection but we currently do not support p{.} and P{.}

    preg_match("/\d*?[[:digit:]]/", $input); // There is an intersection but we currently do not support posix character classes

    preg_match("/\p{L}*?\D/", $input); // There is an intersection but we currently do not support p{.} and P{.}
    preg_match("/\P{L}*?\d/", $input); // There is an intersection but we currently do not support p{.} and P{.}
  }

}
