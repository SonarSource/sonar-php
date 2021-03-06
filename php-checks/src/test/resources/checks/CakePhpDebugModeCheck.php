<?php

use Cake\Core\Configure;

Configure::write('debug', 1); // Noncompliant {{Make sure this debug feature is deactivated before delivering the code in production.}}

configure::Write('debug', 2); // Noncompliant

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

//Named arguments
Configure::write(value: 1, config: 'debug'); // Noncompliant 
Configure::write(value: 0, config: 'debug'); 
Configure::config(engine: true, name: 'debug'); // Noncompliant
Configure::config(engine: false, name: 'debug'); 
Configure::config(engine: true, name: 'xxx'); 
//Mixed named arguments
Configure::write(config: 'debug', 1); // Noncompliant
Configure::config(name: 'debug', false); 
//Coverage
Configure::config(name: 'debug'); //Ok
Configure::config(config: 'debug'); //Ok
