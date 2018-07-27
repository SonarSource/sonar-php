<?php

  setcookie($name, $value, $expire, $path, $domain, $secure, false); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}
//^^^^^^^^^

  setrawcookie($name, $value, $expire, $path, $domain, true, true); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}
//^^^^^^^^^^^^

setcookie($name, $value); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}
setrawcookie($name, $value); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}

setcookie($name, false); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}
setrawcookie($name, false); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}

setcookie($name, 1); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}
setrawcookie($name, 1); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}

setcookie($name, "x"); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}
setrawcookie($name, "x"); // Noncompliant {{Make sure storing this data in this cookie is safe here.}}

setcookie($name); // Ok
setcookie($name, NULL); // Ok
setcookie($name, ""); // Ok
setcookie($name, "   "); // Ok
setrawcookie($name); // Ok
setrawcookie($name, NULL); // Ok
setrawcookie($name, ""); // Ok
setrawcookie($name, "   "); // Ok
