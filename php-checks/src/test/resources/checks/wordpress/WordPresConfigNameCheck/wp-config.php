<?php

  define( 'DISALLOW_FILE_MOD', true ); // Noncompliant {{Unknown WordPress option "DISALLOW_FILE_MOD". Did you mean "DISALLOW_FILE_MODS"?}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
define( 'db_host', true ); // Noncompliant {{Unknown WordPress option "db_host". Did you mean "DB_HOST"?}}

define( 'DISALLOW_FILE_MODS', true ); // Compliant
define( 'DISALLOW_FILE_MOD_123', true ); // Compliant - levenshtein distance > 1
