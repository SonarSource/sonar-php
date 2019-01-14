<?php

use Cake\Routing\Router;
use Cake\Routing as CRouting;

  Router::scope('/', function ($routes) { // Noncompliant
//^^^^^^^^^^^^^
    // ...
});

  router::SCOPE('/', function ($routes) { }); // Noncompliant

Cake\Routing\Router::connect('/', ['controller' => 'MyController', 'action' => 'index']); // Noncompliant

  \Cake\Routing\Router::plugin('MyPlugin', function ($routes) { // Noncompliant
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^
});

CRouting\Router::prefix('admin', function ($routes) { // Noncompliant
    // ...
});

Router::other_method('/', function ($routes) { });

NotARouter::scope('/', function ($routes) { });

Router->scope('/', function ($routes) { });

// coverage
Router["abc"]::scope('/', function ($routes) { });
Router::$scope('/', function ($routes) { });
