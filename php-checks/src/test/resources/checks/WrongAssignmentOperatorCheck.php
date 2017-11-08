<?php

class A {
  private $target = -5;
  private $a;
  private $b;
  private $c;
  private $num = 3;

  function fun() {
    $target =-$num; // Noncompliant {{Was "-=" meant instead?}}
          //^^
    $target = -$num; // Compliant intent to assign inverse value of num is clear
    $target =--$num;

    $target += $num;
    $target =+ $num; // Noncompliant {{Was "+=" meant instead?}}
          //^^
    $target = +$num;
    $target =++$num;
    $target=+$num; // Compliant - no spaces between variable, operator and expression

    $a = $b != $c;
    $a = $b =! $c; // Noncompliant {{Was "!=" meant instead?}}
          //^^
    $a = $b =!! $c; // Noncompliant
          //^^
    $a = $b = !$c;

    $a = &$b;
    $a =&$b;
    $a =& $b; // not detected as this syntax is common
  }
}
