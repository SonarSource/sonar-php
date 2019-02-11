<?php

$a = $_POST['name'];      // Noncompliant {{Do not access "$_POST" directly.}}
//   ^^^^^^
$a = $_COOKIE['name'];    // Noncompliant
$a = $_ENV['name'];       // Noncompliant
$a = $_FILES['name'];     // Noncompliant
$a = $_GET['name'];       // Noncompliant
$a = $_REQUEST['name'];   // Noncompliant
$a = $_SERVER['name'];    // Noncompliant


$b = $_MY_ARRAY['name'];  // OK

$c = $_SESSION['name']; // OK $_SESSION is a special case and can be accessed directly
$c = $GLOBALS['name'];  // OK $GLOBALS does not require sanitization
