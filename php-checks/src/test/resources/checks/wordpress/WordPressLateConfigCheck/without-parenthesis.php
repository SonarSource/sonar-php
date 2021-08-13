<?php

foo();

require_once 'custom-config.php';

define( 'DB_CHARSET', 'utf8' );

require_once ABSPATH . 'wp-settings.php';

define( 'WP_DEBUG', false ); // Noncompliant {{Configuration options at this location will not be taken into account.}}
