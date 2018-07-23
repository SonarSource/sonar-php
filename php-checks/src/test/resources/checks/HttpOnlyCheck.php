<?php
  setcookie($name, $value, $expire, $path, $domain, $secure, false); // Noncompliant {{Set the last argument of "setcookie()" function to "true".}}
//^^^^^^^^^                                                  ^^^^^ <
  setrawcookie($name, $value, $expire, $path, $domain, $secure, false); // Noncompliant {{Set the last argument of "setcookie()" function to "true".}}
//^^^^^^^^^^^^                                                  ^^^^^ <


setcookie($name, $value, $expire, $path, $domain, $secure, foo(false));
setcookie($name, $value, $expire, $path, $domain, $secure, true);
setcookie($name, $value, $expire, $path, $domain, false);
setcookie($name, $value, $expire, $path, $domain, $secure, $httpOnly);
setcookie($name, $value, $expire, $path);

setrawcookie($name, $value, $expire, $path, $domain, $secure, foo(false));
setrawcookie($name, $value, $expire, $path, $domain, $secure, true);
setrawcookie($name, $value, $expire, $path, $domain, false);
setrawcookie($name, $value, $expire, $path, $domain, $secure, $httpOnly);
setrawcookie($name, $value, $expire, $path);
