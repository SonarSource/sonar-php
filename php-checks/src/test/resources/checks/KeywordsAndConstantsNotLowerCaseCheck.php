<?php

require_once "foo.php";   // OK

include_ONCE "bar.php";   // NOK {{Write this "include_ONCE" keyword in lower case.}}

ECHO 'Hello World';       // NOK {{Write this "ECHO" keyword in lower case.}}

$a = NULL;                // NOK {{Write this "NULL" constant in lower case.}}

if ($a == True) {         // NOK {{Write this "True" constant in lower case.}}

} elseif ($a == false) {  // OK

}
