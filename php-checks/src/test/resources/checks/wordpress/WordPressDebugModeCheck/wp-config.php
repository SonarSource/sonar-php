<?php

  define("WP_DEBUG", true); // Noncompliant {{Make sure this debug feature is deactivated before delivering the code in production.}}
//^^^^^^^^^^^^^^^^^^^^^^^^
  define("WP_DEBUG", false);
  define("WP_DEBUG", 1); // Noncompliant
  define("WP-DEBUG", true);
