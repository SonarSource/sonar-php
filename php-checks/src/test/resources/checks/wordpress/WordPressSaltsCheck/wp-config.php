<?php

define( 'AUTH_KEY', 'short' ); // Noncompliant
define( 'AUTH_SALT', 'short' ); // Noncompliant
define( 'LOGGED_IN_KEY', 'short' ); // Noncompliant
define( 'LOGGED_IN_SALT', 'short' ); // Noncompliant
define( 'NONCE_KEY', 'short' ); // Noncompliant
define( 'NONCE_SALT', 'short' ); // Noncompliant
define( 'SECURE_AUTH_KEY', 'short' ); // Noncompliant
define( 'SECURE_AUTH_SALT', 'short' ); // Noncompliant
define( 'AUTH_KEY', 'longlonglonglonglonglonglonglonglonglong' ); // Compliant
define( 'NOT_RELEVANT', 'short' ); // Compliant

define( 'AUTH_KEY', 'put your unique phrase here' ); // Noncompliant
define( 'NOT_RELEVANT', 'put your unique phrase here' ); // Compliant

define( 'AUTH_KEY', '    ' ); // Noncompliant
define( 'NOT_RELEVANT', '    ' ); // Compliant
