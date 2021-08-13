<?php

define( 'WP_HTTP_BLOCK_EXTERNAL', false ); // Noncompliant {{Make sure allowing external requests is intended.}}
define( 'WP_HTTP_BLOCK_EXTERNAL', 0 ); // Noncompliant

define( 'WP_HTTP_BLOCK_EXTERNAL', true ); // Compliant
define( 'WP_HTTP_BLOCK_EXTERNAL', getenv("BLOCK_EXTERNAL") ); // Compliant
