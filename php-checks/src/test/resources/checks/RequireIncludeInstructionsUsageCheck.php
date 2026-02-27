<?php

use Shop\Vegetable\Entity\Tomato;

foo(); // Compliant

include "./requireIncludeUsage/WithNamespace.php"; // Noncompliant {{Replace "include" with namespace import mechanism through the "use" keyword.}}
INCLUDE "./requireIncludeUsage/WithNamespace.php"; // Noncompliant {{Replace "INCLUDE" with namespace import mechanism through the "use" keyword.}}
include("./requireIncludeUsage/WithNamespace.php"); // Noncompliant {{Replace "include" with namespace import mechanism through the "use" keyword.}}
require "./requireIncludeUsage/WithNamespace.php"; // Noncompliant {{Replace "require" with namespace import mechanism through the "use" keyword.}}
require("./requireIncludeUsage/WithNamespace.php"); // Noncompliant {{Replace "require" with namespace import mechanism through the "use" keyword.}}
include_once "./requireIncludeUsage/WithNamespace.php"; // Noncompliant {{Replace "include_once" with namespace import mechanism through the "use" keyword.}}
include_once("./requireIncludeUsage/WithNamespace.php"); // Noncompliant {{Replace "include_once" with namespace import mechanism through the "use" keyword.}}
require_once "./requireIncludeUsage/WithNamespace.php"; // Noncompliant {{Replace "require_once" with namespace import mechanism through the "use" keyword.}}
require_once("./requireIncludeUsage/WithNamespace.php"); // Noncompliant {{Replace "require_once" with namespace import mechanism through the "use" keyword.}}

// No namespace in included file — Compliant
include "./requireIncludeUsage/WithoutNamespace.php"; // Compliant
require "./requireIncludeUsage/WithoutNamespace.php"; // Compliant

require 'vendor/autoload.php'; // Compliant
include 'vendor/autoload.php'; // Compliant
require __DIR__ . '/vendor/autoload.php'; // Compliant

// Return value used - Compliant
$config = require './requireIncludeUsage/WithNamespace.php'; // Compliant
$data = include './requireIncludeUsage/WithNamespace.php'; // Compliant
return require './requireIncludeUsage/WithNamespace.php'; // Compliant
foo(require './requireIncludeUsage/WithNamespace.php'); // Compliant

// Non-PHP extensions - Compliant
include 'template.html'; // Compliant
require 'layout.twig'; // Compliant
include_once 'data.json'; // Compliant
