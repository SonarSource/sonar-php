<?php

$GLOBALS['G'];

if ($HTTP_POST_VARS["name"]) {  // Noncompliant {{Replace this use of the deprecated "$HTTP_POST_VARS" variable with "$_POST".}}
//  ^^^^^^^^^^^^^^^
}

call($HTTP_GET_VARS["name"]);   // Noncompliant {{Replace this use of the deprecated "$HTTP_GET_VARS" variable with "$_GET".}}

${$HTTP_POST_FILES["name"]};    // Noncompliant

$$HTTP_SERVER_VARS["name"];     // Noncompliant

$var::$HTTP_POST_FILES["name"]; // OK

$var->$HTTP_POST_FILES["name"]; // OK

echo $php_errormsg; // Noncompliant
