<?php
  setcookie($name, $value, $expire, $path, $domain, $secure, false); // Noncompliant {{Set the last argument of "setcookie()" function to "true".}}
//^^^^^^^^^                                                  ^^^^^ <


setcookie($name, $value, $expire, $path, $domain, $secure, foo(false));
setcookie($name, $value, $expire, $path, $domain, $secure, true);
setcookie($name, $value, $expire, $path, $domain, false);
setcookie($name, $value, $expire, $path, $domain, $secure, $httpOnly);
setcookie($name, $value, $expire, $path);
