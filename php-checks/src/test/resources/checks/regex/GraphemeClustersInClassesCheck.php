<?php

namespace checks\regex;

class GraphemeClustersInClassesCheck
{
  function noncompliant($input) {
    preg_match("/[aaaèaaa]/", $input); // Noncompliant {{Extract 1 Grapheme Cluster(s) from this character class.}}
    preg_match("/[0Ṩ0]/", $input); // Noncompliant {{Extract 1 Grapheme Cluster(s) from this character class.}}
    preg_match("/aaa[è]aaa/", $input); // Noncompliant
    // two secondary per line: one for the regex location, and one for the cluster location
    preg_match("/[èaaèaaè]/", $input); // Noncompliant {{Extract 3 Grapheme Cluster(s) from this character class.}}
    preg_match("/[èa-dä]/", $input); // Noncompliant
    preg_match("/[èa]aaa[dè]/", $input);     // Noncompliant 2
    preg_match("/[ä]/", $input); // Noncompliant
    preg_match("/[c̈]/", $input); // Noncompliant
    preg_match("/[e⃝]/", $input); // Noncompliant
  }

  function compliant($input) {
    preg_match("/[é]/", $input); // Compliant, a single char
    preg_match("/[e\u0300]/", $input); // Compliant, escaped unicode
    preg_match("/[e\x{0300}]/", $input); // Compliant, escaped unicode
    preg_match("/[e\u20DD̀]/", $input); // Compliant, (letter, escaped unicode, mark) can not be combined
    preg_match("/[\u0300e]/", $input); // Compliant, escaped unicode, letter
    preg_match("/[̀̀]/", $input); // Compliant, two marks
    preg_match("/[̀̀]/", $input); // Compliant, one mark

    preg_match("/ä/", $input); // Compliant, not in a class
  }
}
