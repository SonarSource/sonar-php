<?php

namespace checks\regex;

class GroupReplacementCheck
{
  function noncompliant() {
    preg_replace("/(a)/", "$2", ""); // Noncompliant {{Referencing non-existing group: 2.}}
    //                    ^^^^
    preg_replace("/(a)/", '${2}', ""); // Noncompliant
    preg_replace("/a/", "$1", ""); // Noncompliant
    preg_replace("/(?!a)/", "$1", ""); // Noncompliant
    preg_replace("/(a)/", '\2', ""); // Noncompliant
    preg_replace("/(a)/", "$1 $2", ""); // Noncompliant
    preg_replace("/(a)/", "$3 $2", ""); // Noncompliant {{Referencing non-existing groups: 3, 2.}}
    preg_replace("/(a)/", "$2 \1", ""); // Noncompliant
  }

  function compliant() {
    preg_replace("/(a)/", "$0", "");
    preg_replace("/(a)/", "$1", "");
    preg_replace("/(a)/", '${1}', "");
    preg_replace("/(a)/", "\1", "");
    preg_replace("/(a)(b)/", "\1 \2", "");
    preg_replace("/(a(b))/", "\1 \2", "");
  }
}
