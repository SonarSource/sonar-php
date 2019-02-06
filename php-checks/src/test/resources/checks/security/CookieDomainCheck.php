<?php

session_set_cookie_params(3600, "/~path/", ".com"); // Noncompliant
Session_Set_Cookie_Params(3600, "/~path/", ".com"); // Noncompliant
setcookie("TestCookie", $value, time()+3600, "/~path/", ".com", 1); // Noncompliant

setCookie("TestCookie", $value, time()+3600, "/~path/", ".com", 1); // Noncompliant {{Specify at least a second-level cookie domain.}}
//                                                      ^^^^^^

setcookie("unique2u", "SESSION-123456678", null, null, null, null, false); // Compliant - blank value is allowed

session_set_cookie_params(3600, "/~path/", ".com"); // Noncompliant

$domainOne = ".com";  // Noncompliant
//           ^^^^^^
setcookie("TestCookie", $value, time()+3600, "/~path/", $domainOne, 1);
//                                                      ^^^^^^^^^^<

$domainTwo = ".ch"; // Noncompliant
//           ^^^^^
session_set_cookie_params(3600, "/~path/", $domainTwo);
//                                         ^^^^^^^^^^<

setcookie("TestCookie", $value, time()+3600, "/~path/", ".myDomain.com", 1);
session_set_cookie_params(3600, "/~path/", ".myDomain.com");
session_set_cookie_params(3600, "/~path/", "foo.myDomain.com");

$domain3 = "foo.bar";
setcookie("TestCookie", $value, time()+3600, "/~path/", $domain3, 1);
session_set_cookie_params(3600, "/~path/", $domain3);

// special values
session_set_cookie_params(3600, "/~path/", "................."); // Noncompliant
session_set_cookie_params(3600, "/~path/", ""); // Compliant
session_set_cookie_params(3600, "/~path/", "-1"); // Noncompliant
session_set_cookie_params(3600, "/~path/", NULL); // Compliant

// no domain parameter
setcookie("TestCookie");
session_set_cookie_params(3600);

$notUsed = ".com"; // ignored
