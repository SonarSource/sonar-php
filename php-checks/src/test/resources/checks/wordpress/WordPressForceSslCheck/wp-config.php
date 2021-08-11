<?php


  define("FORCE_SSL_ADMIN", false); // Noncompliant {{Using non SSL protocol is insecure. Force using SSL protocol instead.}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  define("FORCE_SSL_ADMIN", true);
  define("FORCE_SSL_ADMIN", 0); // Noncompliant
  define("force_ssl_admin", false); // Noncompliant

  define("FORCE_SSL_ADMIN", false); // Noncompliant
  define("FORCE_SSL_ADMIN", true);
  define("FORCE_SSL_ADMIN", 0); // Noncompliant
  define("force_ssl_admin", false); // Noncompliant

  define("FOOBAR", false);
