<?php

// The following 2 lines are non-compliant
$a = b + c; // This is a trailing comment that can be very very long
$a = b + c; # Shell-style comment

// This very long comment is better placed before the line of code
$a = /* not a trailing comment */ b + c;

// The following 2 lines are compliant with the default configuration of the rule
$a = "id"; // $NON-NLS-1$
$a = "id"; # $NON-NLS-1$
