<?php

  define("WP_ALLOW_REPAIR", true); // Noncompliant {{Make sure allowing unauthenticated database repair is intended.}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  define("WP_ALLOW_REPAIR", false);
  define("WP_ALLOW_REPAIR", 1); // Noncompliant
  define("WP_ALLOW_REPAIR", $unknown);
