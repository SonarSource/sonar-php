<?php

function noncompliantCharRanges() {
  preg_match("/[a-z]/", $input); // Noncompliant {{Replace this character range with a Unicode-aware character class.}}
  //            ^^^
  preg_match("/[A-Z]/", $input); // Noncompliant
  preg_match("/[0-9a-z]/", $input); // Noncompliant
  preg_match("/[abcA-Zdef]/", $input); // Noncompliant
  preg_match("/[\\x{61}-\\x{7A}]/", $input); // Noncompliant
  //            ^^^^^^^^^^^^^^^
  preg_match("/[a-zA-Z]/", $input); // Noncompliant {{Replace these character ranges with Unicode-aware character classes.}}
}

function noncompliantPredefinedPosixClasses() {
  preg_match("/\\s/", $input); // Noncompliant
  preg_match("/\\S/", $input); // Noncompliant
  preg_match("/\\w/", $input); // Noncompliant
  preg_match("/\\W/", $input); // Noncompliant
}

function compliantCharRanges() {
  preg_match("/[0-9]/", $input); // Compliant: we do not consider digits
  preg_match("/[a-y]/", $input); // Compliant: It appears a more restrictive range than simply 'all letters'
  preg_match("/[D-Z]/", $input);
  preg_match("/[\\x{1F600}-\\x{1F637}]/", $input);
}

function compliantPredefinedPosixClasses() {
  preg_match("/\\w/u", $input);
  preg_match("/(?U)\\w/", $input);
  preg_match("/(?U:\\w)/", $input);
  preg_match("/\\w/iu",);
  preg_match("/\\w((?U)\\w)\\w/", $input);
  preg_match("/\\w(?U:[a-y])\\w/", $input); // Compliant. We assume the developer knows what they are doing if they are using unicode flags somewhere.
}
