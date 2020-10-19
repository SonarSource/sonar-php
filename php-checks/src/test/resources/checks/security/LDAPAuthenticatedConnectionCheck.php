<?php

$ldapconn = ldap_connect("foo.com");

$ldapbind = ldap_bind($ldapconn); // Noncompliant {{Provide username and password to authenticate the connection.}}
//          ^^^^^^^^^^^^^^^^^^^^

  LDAP_BIND($ldapconn, "username"); // Noncompliant
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

ldap_bind(link_identifier:$ldapconn, bind_rdn:"username", bind_password:"password");
ldap_bind(link_identifier:"", bind_rdn:"username", bind_password:"password");
ldap_bind(bind_password:"password", bind_rdn:"username");
ldap_bind(bind_password:"", bind_rdn:"username", link_identifier:$ldapconn); // Noncompliant
ldap_bind(link_identifier:$ldapconn, bind_rdn:"username"); // Noncompliant
ldap_bind(link_identifier:$ldapconn, bind_password:"password"); // Noncompliant

function foobar($cond) {
  $g = "";
  $h = "";
  if ($cond) {
    $g = "user";
    $h = "pass";
  }
  ldap_bind($ldapconn, $g, $h); // FN
}
