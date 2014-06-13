<?php
require_once "foo.php";
include_ONCE "bar.php";

$a = NULL;                // NOK

ECHO 'Hello World';       // NOK

if ($a == True) {         // NOK

} elseif ($a == false) {  // OK

}
