<?php

$GLOBALS['G'];

if ($HTTP_POST_VARS["name"]) {  // NOK {{Replace this use of the deprecated "$HTTP_POST_VARS" variable with "$_POST".}}
}

call($HTTP_GET_VARS["name"]);   // NOK {{Replace this use of the deprecated "$HTTP_GET_VARS" variable with "$_GET".}}

${$HTTP_POST_FILES["name"]};    // NOK

$$HTTP_SERVER_VARS["name"];     // NOK

$var::$HTTP_POST_FILES["name"]; // OK

$var->$HTTP_POST_FILES["name"]; // OK

