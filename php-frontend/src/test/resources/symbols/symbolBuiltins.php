<?php

$myvar = 1;
$argc = 1;
$_SERVER = [];
$ARGV = [];

print_r($myvar);
print_r($GLOBALS);
print_r($_SERVER);
print_r($_GET);
print_r($_POST);
print_r($_FILES);
print_r($_SESSION);
print_r($_ENV);
print_r($PHP_ERRORMSG);
print_r($HTTP_RAW_POST_DATA);
print_r($HTTP_RESPONSE_HEADER);
print_r($argc);
print_r($_COOKIE);
print_r($_REQUEST);

function foo($argv) {
  $_SERVER = [];
  $argc = 0;
  echo $argc;
  echo $argv;
}

print_r($myvar);

class A {
    function __construct() {
        $myvar = 0;
        print_r($this);
    }

    function foo($myvar) {
        $this.foo();
    }
}
