<?php

  define( 'WP_AUTO_UPDATE_CORE', false ); // Noncompliant {{Make sure deactivating automatic updates is intended.}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
define( 'AUTOMATIC_UPDATER_DISABLED', true ); // Noncompliant
define( 'DISALLOW_FILE_MODS', true ); // Noncompliant

define( 'WP_AUTO_UPDATE_CORE', true ); // Compliant
define( 'WP_AUTO_UPDATE_CORE', 'minor' ); // Compliant
define( 'AUTOMATIC_UPDATER_DISABLED', false ); // Compliant
