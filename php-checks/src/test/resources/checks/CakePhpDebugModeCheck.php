<?php

use Cake\Core\Configure;

Configure::write('debug', 1); // Noncompliant {{Make sure CakePHP's debug mode is not activated on production code.}}

Configure::write('debug', 2); // Noncompliant

Configure::write('debug', 3); // Noncompliant

Configure::config('debug', true); // Noncompliant


Configure::write('debug', 0);
Configure::config('debug', false);
Configure::config('App.baseUrl', "http://example.com/");
