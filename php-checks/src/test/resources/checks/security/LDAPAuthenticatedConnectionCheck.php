<?php

$ldapconn = ldap_connect("foo.com");

$ldapbind = ldap_bind($ldapconn); // Noncompliant {{Provide username and password to authenticate the connection.}}
//          ^^^^^^^^^^^^^^^^^^^^

  ldap_bind($ldapconn, "username"); // Noncompliant
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

  ldap_bind($ldapconn, NULL, NULL); // Noncompliant
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

ldap_bind($ldapconn, "username", ""); // Noncompliant

$a = "";
$b = "";
ldap_bind($ldapconn, $a, $b); // Noncompliant

$c = "username";
$d = "";
ldap_bind($ldapconn, $c, $d); // Noncompliant

$e = "user";
$f = "pass";
ldap_bind($ldapconn, $e, $f);


ldap_bind($ldapconn, "username", "password");
ldap_bind($ldapconn, $username, $password); // unknown variables

function foobar($cond) {
  $g = "";
  $h = "";
  if ($cond) {
    $g = "user";
    $h = "pass";
  }
  ldap_bind($ldapconn, $g, $h); // FN
}
