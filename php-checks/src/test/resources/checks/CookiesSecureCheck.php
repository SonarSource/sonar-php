<?php
  setcookie($name, $value, $expire, $path, $domain, false); // Noncompliant {{Make sure creating this cookie without the "secure" flag is safe here.}}
//^^^^^^^^^

  setcookie($name, $value, $expire, $path, $domain, false, true); // Noncompliant {{Make sure creating this cookie without the "secure" flag is safe here.}}
//^^^^^^^^^

setcookie($name, $value, $expire, $path, $domain, FALSE); // Noncompliant
setcookie($name, $value, $expire, $path, $domain, False); // Noncompliant
setcookie($name, $value, $expire, $path, $domain, 0); // Noncompliant
setcookie($name, $value, $expire, $path, $domain, ""); // Noncompliant

setcookie($name, $value, $expire, $path, $domain, $secure, false);
setcookie($name, $value, $expire, $path, $domain, true);
setcookie($name, $value, $expire, $path, $domain, foo(false));
setcookie($name, $value, $expire, $path, $domain, $secure);
setcookie($name, $value, $expire, $path); // Noncompliant

session_set_cookie_params($lifetime, $path, $domain, false); // Noncompliant
session_set_cookie_params($lifetime, $path, $domain); // Noncompliant
session_set_cookie_params($lifetime, $path, $domain, true);
