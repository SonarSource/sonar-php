<?php

include "file.php";       // NOK {{Replace "include" with "include_once".}}
require "file.php";       // NOK {{Replace "require" with "require_once".}}

include_once "file.php";  // OK
require_once "file.php";  // OK
