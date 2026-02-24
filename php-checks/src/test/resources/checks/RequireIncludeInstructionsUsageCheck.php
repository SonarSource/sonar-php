<?php

use Drupal\vegetable\Entity\Tomato;

foo(); // Compliant

include "./modules/vegetable/src/Entity/Tomato.php"; // Noncompliant {{Replace "include" with namespace import mechanism through the "use" keyword.}}
INCLUDE "./modules/vegetable/src/Entity/Tomato.php"; // Noncompliant {{Replace "INCLUDE" with namespace import mechanism through the "use" keyword.}}
include("./modules/vegetable/src/Entity/Tomato.php"); // Noncompliant {{Replace "include" with namespace import mechanism through the "use" keyword.}}
require "./modules/vegetable/src/Entity/Tomato.php"; // Noncompliant {{Replace "require" with namespace import mechanism through the "use" keyword.}}
require("./modules/vegetable/src/Entity/Tomato.php"); // Noncompliant {{Replace "require" with namespace import mechanism through the "use" keyword.}}
include_once "./modules/vegetable/src/Entity/Tomato.php"; // Noncompliant {{Replace "include_once" with namespace import mechanism through the "use" keyword.}}
include_once("./modules/vegetable/src/Entity/Tomato.php"); // Noncompliant {{Replace "include_once" with namespace import mechanism through the "use" keyword.}}
require_once "./modules/vegetable/src/Entity/Tomato.php"; // Noncompliant {{Replace "require_once" with namespace import mechanism through the "use" keyword.}}
require_once("./modules/vegetable/src/Entity/Tomato.php"); // Noncompliant {{Replace "require_once" with namespace import mechanism through the "use" keyword.}}

require 'vendor/autoload.php'; // Compliant
include 'vendor/autoload.php'; // Compliant
require __DIR__ . '/vendor/autoload.php'; // Compliant

// Return value used - Compliant
$config = require 'config.php'; // Compliant
$data = include 'data.php'; // Compliant
return require 'bootstrap.php'; // Compliant
foo(require 'helper.php'); // Compliant

// Non-PHP extensions - Compliant
include 'template.html'; // Compliant
require 'layout.twig'; // Compliant
include_once 'data.json'; // Compliant
