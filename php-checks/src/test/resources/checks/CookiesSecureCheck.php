<?php
  setcookie($name, $value, $expire, $path, $domain, false); // Noncompliant {{Set the 6th argument of "setcookie()" function to "true".}}
//^^^^^^^^^                                         ^^^^^ <

  setcookie($name, $value, $expire, $path, $domain, false, true); // Noncompliant {{Set the 6th argument of "setcookie()" function to "true".}}
//^^^^^^^^^                                         ^^^^^ <

setcookie($name, $value, $expire, $path, $domain, $secure, false);
setcookie($name, $value, $expire, $path, $domain, true);
setcookie($name, $value, $expire, $path, $domain, foo(false));
setcookie($name, $value, $expire, $path, $domain, $secure);
setcookie($name, $value, $expire, $path); // Noncompliant
