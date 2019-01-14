<?php

function globfunc() {
    global $argv; // Noncompliant {{Make sure that command line arguments are used safely here.}}
//         ^^^^^
    foreach ($argv as $arg) { // Noncompliant
//           ^^^^^
    }
}

function myfunc($argv) {
    $param = $argv[0]; // OK. Reference to local $argv parameter
    // ...
}

foreach ($argv as $arg) { // Noncompliant
    // ...
}

$myargv = $_SERVER['argv']; // Noncompliant
//        ^^^^^^^^^^^^^^^^
$_SERVER['other_variable'];

function serve() {
    $myargv = $_SERVER['argv']; // Noncompliant
    // ...
}

myfunc($argv); // Noncompliant

$myvar = $HTTP_SERVER_VARS[0]; // Noncompliant
//       ^^^^^^^^^^^^^^^^^

$options = getopt('a:b:'); // Noncompliant
$options = GetOpt('a:b:'); // Noncompliant

$GLOBALS["argv"]; // Noncompliant

function myglobals() {
    $GLOBALS["argv"]; // Noncompliant
//  ^^^^^^^^^^^^^^^^
    $GLOBALS["abcdef"];
}

$argv = [1,2,3]; // Noncompliant

new Zend\Console\Getopt(['myopt|m' => 'this is an option']); // Noncompliant
new zend\console\getopt(['myopt|m' => 'this is an option']); // Noncompliant

new \GetOpt\Option('m', 'myoption', \GetOpt\GetOpt::REQUIRED_ARGUMENT); // Noncompliant
new \getopt\option('m', 'myoption', \GetOpt\GetOpt::REQUIRED_ARGUMENT); // Noncompliant

// coverage
getopt["abc"]();
abcd();
$_SERVER[];
$_NOT_SERVER['argv'];

function abc() {
    global ${$bcde};
}
