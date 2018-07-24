<?php

use Cake\Core\Configure;

Configure::write('debug', 1); // Noncompliant {{Make sure CakePHP's debug mode is not activated on production code.}}

Configure::write('debug', 2); // Noncompliant

Configure::write('debug', 3); // Noncompliant

Configure::config('debug', true); // Noncompliant

$debug = true;
Configure::config('debug', $debug); // Noncompliant


Configure::write('debug', 0);
Configure::config('debug', false);
Configure::config('App.baseUrl', "http://example.com/");
$debug2 = false;
Configure::config('debug', $debug2);

Configure::write('xxx', 1);
Configure::config('xxx', true);
Configure::write($var, 1);
Configure::write('debug', $var);
Configure::xxx('debug', 1);

$debug3 = doSomething();
Configure::config('debug', $debug3);
