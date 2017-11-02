<?php

  include "file.php";       // Noncompliant {{Replace "include" with "include_once".}}
//^^^^^^^
  require "file.php";       // Noncompliant {{Replace "require" with "require_once".}}
//^^^^^^^

include_once "file.php";  // OK
require_once "file.php";  // OK
