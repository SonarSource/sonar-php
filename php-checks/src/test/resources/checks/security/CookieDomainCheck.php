<?php

session_set_cookie_params(3600, "/~path/", ""); // Noncompliant
setcookie("TestCookie", $value, time()+3600, "/~path/", "", 1); // Noncompliant

setcookie("TestCookie", $value, time()+3600, "/~path/", ".com", 1); // Noncompliant {{Specify at least a second-level cookie domain.}}
//                                                      ^^^^^^
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
session_set_cookie_params(3600, "/~path/", ""); // Noncompliant
session_set_cookie_params(3600, "/~path/", "-1"); // Noncompliant
session_set_cookie_params(3600, "/~path/", NULL); // Noncompliant

// no domain parameter
setcookie("TestCookie");
session_set_cookie_params(3600);

$notUsed = ".com"; // ignored
