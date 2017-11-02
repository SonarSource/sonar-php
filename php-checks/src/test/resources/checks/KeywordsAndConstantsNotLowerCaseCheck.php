<?php

require_once "foo.php";   // OK

  include_ONCE "bar.php";   // Noncompliant {{Write this "include_ONCE" keyword in lower case.}}
//^^^^^^^^^^^^

  ECHO 'Hello World';       // Noncompliant {{Write this "ECHO" keyword in lower case.}}
//^^^^
$a = NULL;                // Noncompliant {{Write this "NULL" constant in lower case.}}
//   ^^^^

if ($a == True) {         // Noncompliant {{Write this "True" constant in lower case.}}

} elseif ($a == false) {  // OK

}
