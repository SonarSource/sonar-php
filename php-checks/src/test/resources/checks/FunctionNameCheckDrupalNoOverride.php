<?php
use Drupal\something;

function __construct() {  // OK
}

function __destruct() {   // OK
}

function doSomething() {  // OK
}

function DoSomething() {  // OK
}

// Non compliant: valid for drupal, but regex was overridden, so drupal regex does not apply.
function do_something() {  // Noncompliant {{Rename function "do_something" to match the regular expression ^[a-zA-Z][a-zA-Z0-9]*$.}}
//       ^^^^^^^^^^^^
}
