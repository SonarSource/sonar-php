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
