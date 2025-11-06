<?php

use Illuminate\Http\Request;

foo(); // Compliant

include "./modules/vegetable/src/Entity/Tomato.php"; // Compliant
INCLUDE "./modules/vegetable/src/Entity/Tomato.php"; // Compliant
include("./modules/vegetable/src/Entity/Tomato.php"); // Compliant
require "./modules/vegetable/src/Entity/Tomato.php"; // Compliant
require("./modules/vegetable/src/Entity/Tomato.php"); // Compliant
include_once "./modules/vegetable/src/Entity/Tomato.php"; // Compliant
include_once("./modules/vegetable/src/Entity/Tomato.php"); // Compliant
require_once "./modules/vegetable/src/Entity/Tomato.php"; // Compliant
require_once("./modules/vegetable/src/Entity/Tomato.php"); // Compliant

require 'vendor/autoload.php'; // Compliant
include 'vendor/autoload.php'; // Compliant
require __DIR__ . '/vendor/autoload.php'; // Compliant
