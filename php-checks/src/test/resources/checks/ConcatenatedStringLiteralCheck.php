<?php

$a = "str1" . "str2";                // Noncompliant {{Combine these strings instead of concatenating them.}}
//   ^^^^^^   ^^^^^^<
$a = "str1" . "$a";                // Noncompliant
//   ^^^^^^
$b = "str1" . $a . "str2" . "str3";  // Noncompliant

$b = "str1" . "str2" . $a . "str3";  // Noncompliant
$a = "str1"                          // Noncompliant
. "str2"
. "str3"
. "str4";


$a = $b
."str1"                          // Noncompliant
. "str2"
. "str3"
. "str4";

$b = "str" . $a;                    // OK
$b = "str1" . $a . "str2";          // OK


