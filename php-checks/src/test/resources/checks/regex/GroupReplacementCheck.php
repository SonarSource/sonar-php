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
    preg_replace("/(a)/", '\02', "");
    preg_replace("/(a)(b)/", "\1 \2", "");
    preg_replace("/(a(b))/", "\1 \2", "");
    preg_replace('/(\"(\w+)\"\s?:)/im', "\"\033[36m$2\033[0m\":", $variable);
  }

  function unresolved_pattern() {
    preg_replace($this->patterns->getSimpleEscapePattern(), '$1', "");
    preg_replace(['/([A-Z]+)([A-Z][a-z])/', '/([a-z\d])([A-Z])/'], '\1_\2', "");
    preg_replace($regex, '<a href="http$1://$2$3$4" class="_blanktarget">$0</a>', "");
    preg_replace('/&lt;'. $name .'([^&]+)+\&gt;/', '<span \\1>', "");
    preg_replace('/(?<=\\pL)(\\p{Lu})/u', '_\\1', ""); // Parsing error on the regex should not cause an issue
  }
}
