<?php

$a = "str1" . "str2";                // NOK
$b = "str1" . $a . "str2" . "str3";  // NOK
$a = "str1"                          // NOK
. "str2"
. "str3"
. "str4";

$b = "str" . $a;                    // OK
$b = "str1" . $a . "str2";          // OK


