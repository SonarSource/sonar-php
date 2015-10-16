<?php

$a = "str1" . "str2";                // NOK {{Combine these strings instead of concatenating them.}}
$a = "str1" . "$a";                // NOK
$b = "str1" . $a . "str2" . "str3";  // NOK
$b = "str1" . "str2" . $a . "str3";  // NOK
$a = "str1"                          // NOK
. "str2"
. "str3"
. "str4";


$a = $b
."str1"                          // NOK
. "str2"
. "str3"
. "str4";

$b = "str" . $a;                    // OK
$b = "str1" . $a . "str2";          // OK


