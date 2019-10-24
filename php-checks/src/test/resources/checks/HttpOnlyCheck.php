<?php

  setcookie($name, $value, $expire, $path, $domain, true, false); // Noncompliant {{Make sure creating this cookie without the "httpOnly" flag is safe here.}}
//^^^^^^^^^                                               ^^^^^ <
  setrawcookie($name, $value, $expire, $path, $domain, true, false); // Noncompliant {{Make sure creating this cookie without the "httpOnly" flag is safe here.}}
//^^^^^^^^^^^^                                               ^^^^^ <
SetRawCookie($name, $value, $expire, $path, $domain, true, false); // Noncompliant

setcookie($name, $value, $expire, $path, $domain, true, foo(false));
setcookie($name, $value, $expire, $path, $domain, true, true);
setcookie($name, $value, $expire, $path, $domain, false); // Noncompliant
setcookie($name, $value, $expire, $path, $domain, true, $httpOnly);
setcookie($name, $value, $expire, $path);  // Noncompliant
setcookie($name, $value); // Noncompliant

setrawcookie($name, $value, $expire, $path, $domain, true, foo(false));
setrawcookie($name, $value, $expire, $path, $domain, true, true);
setrawcookie($name, $value, $expire, $path, $domain, false);  // Noncompliant
setrawcookie($name, $value, $expire, $path, $domain, true, $httpOnly);
setrawcookie($name, $value, $expire, $path);  // Noncompliant
setrawcookie($name, $value);  // Noncompliant


$params = session_get_cookie_params();
setcookie($name, $value, $params); // Compliant

$expires = 42;
setcookie($name, $value, $expires); // false negative

