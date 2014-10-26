<?php

include "file.php";       // NOK
require "file.php";       // NOK

include_once "file.php";  // OK
require_once "file.php";  // OK
