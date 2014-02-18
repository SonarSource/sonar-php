<?php

$GLOBALS['G'];

if ($HTTP_POST_VARS["name"]) {  // NOK
}

call($HTTP_GET_VARS["name"]);   // NOK

$var::$HTTP_POST_FILES["name"]; // OK

$var->$HTTP_POST_FILES["name"]; // OK

${$HTTP_POST_FILES["name"]};    // OK

$$HTTP_SERVER_VARS["name"];     // OK
