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

  setcookie($value, $expire, $path, $domain, httponly: false, expires: true, name: $name); // Noncompliant {{Make sure creating this cookie without the "httpOnly" flag is safe here.}}
//^^^^^^^^^                                            ^^^^^ <
  setrawcookie($name, httponly: false); // Noncompliant {{Make sure creating this cookie without the "httpOnly" flag is safe here.}}
//^^^^^^^^^^^^                  ^^^^^ <

  setcookie(httponly: foo(false));
  setrawcookie(httponly: $httpOnly, name: $name);


use Symfony\Component\HttpFoundation\Cookie;

  Cookie::create('name', 'value', $minutes, $path, $domain, $secure, FALSE); // Noncompliant {{Make sure creating this cookie without the "httpOnly" flag is safe here.}}
//^^^^^^^^^^^^^^                                                     ^^^^^ <
Cookie::create('name', 'value', $minutes, $path, $domain, $secure, TRUE);
Cookie::create('name', 'value', $minutes, $path, $domain, $secure, $httpOnly);
Cookie::create('name', 'value', $minutes, $path, $domain, $secure);

  Cookie::create(name:'name', value:'value', httpOnly:FALSE); // Noncompliant
//^^^^^^^^^^^^^^                                      ^^^^^ <

Cookie::otherMethod('name', 'value', $minutes, $path, $domain, $secure, FALSE);
OtherClass::create('name', 'value', $minutes, $path, $domain, $secure, FALSE);
$otherClass::create('name', 'value', $minutes, $path, $domain, $secure, FALSE);

new Cookie($name, $value, $expire, $path, $domain, $secure, FALSE); // Noncompliant
//  ^^^^^^                                                  ^^^^^ <
new Cookie(httpOnly: FALSE); // Noncompliant

new Cookie($name, $value, $expire, $path, $domain, $secure, TRUE);
new Cookie($name, $value, $expire, $path, $domain, $secure);

use my\other\HttpFoundation\Cookie;
new Cookie($name, $value, $expire, $path, $domain, $secure, FALSE);
