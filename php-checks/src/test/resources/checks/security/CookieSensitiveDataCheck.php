<?php

  setcookie($name, $value, $expire, $path, $domain, $secure, false); // Noncompliant {{Make sure that this cookie is written safely.}}
//^^^^^^^^^

  setrawcookie($name, $value, $expire, $path, $domain, true, true); // Noncompliant {{Make sure that this cookie is written safely.}}
//^^^^^^^^^^^^

setcookie($name, $value); // Noncompliant {{Make sure that this cookie is written safely.}}
setrawcookie($name, $value); // Noncompliant {{Make sure that this cookie is written safely.}}

SetCookie($name, false); // Noncompliant {{Make sure that this cookie is written safely.}}
SetRawCookie($name, false); // Noncompliant {{Make sure that this cookie is written safely.}}

setcookie($name, 1); // Noncompliant {{Make sure that this cookie is written safely.}}
setrawcookie($name, 1); // Noncompliant {{Make sure that this cookie is written safely.}}

setcookie($name, "x"); // Noncompliant {{Make sure that this cookie is written safely.}}
setrawcookie($name, "x"); // Noncompliant {{Make sure that this cookie is written safely.}}

setcookie($name); // Ok
setcookie($name, NULL); // Ok
setcookie($name, ""); // Ok
setcookie($name, "   "); // Ok
setrawcookie($name); // Ok
setrawcookie($name, NULL); // Ok
setrawcookie($name, ""); // Ok
setrawcookie($name, "   "); // Ok
