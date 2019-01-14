<?php

  setcookie($name, $value, $expire, $path, $domain, $secure, false); // Noncompliant {{Make sure that this cookie is used safely.}}
//^^^^^^^^^

  setrawcookie($name, $value, $expire, $path, $domain, true, true); // Noncompliant {{Make sure that this cookie is used safely.}}
//^^^^^^^^^^^^

setcookie($name, $value); // Noncompliant {{Make sure that this cookie is used safely.}}
setrawcookie($name, $value); // Noncompliant {{Make sure that this cookie is used safely.}}

SetCookie($name, false); // Noncompliant {{Make sure that this cookie is used safely.}}
SetRawCookie($name, false); // Noncompliant {{Make sure that this cookie is used safely.}}

setcookie($name, 1); // Noncompliant {{Make sure that this cookie is used safely.}}
setrawcookie($name, 1); // Noncompliant {{Make sure that this cookie is used safely.}}

setcookie($name, "x"); // Noncompliant {{Make sure that this cookie is used safely.}}
setrawcookie($name, "x"); // Noncompliant {{Make sure that this cookie is used safely.}}

setcookie($name); // Ok
setcookie($name, NULL); // Ok
setcookie($name, ""); // Ok
setcookie($name, "   "); // Ok
setrawcookie($name); // Ok
setrawcookie($name, NULL); // Ok
setrawcookie($name, ""); // Ok
setrawcookie($name, "   "); // Ok

  $_COOKIE["name"]; // Noncompliant
//^^^^^^^^
  $HTTP_COOKIE_VARS["name"]; // Noncompliant
//^^^^^^^^^^^^^^^^^
$abc = $_COOKIE["name"]; // Noncompliant

unset($_COOKIE["name"]);
unset($HTTP_COOKIE_VARS["name"]);
isset($_COOKIE["name"]);
isset($HTTP_COOKIE_VARS["name"]);

other_function($_COOKIE["name"]); // Noncompliant
