<?php
$x = "http://www.mywebsite.com";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
//   ^^^^^^^^^^^^^^^^^^^^^^^^^^

$y = "ftp://myserver.com";   // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}

$z = "~/a";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$z = "/Folder/";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$z = "/folder/file.txt";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$z = "//my-network-drive/file.txt";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$z = "//my-network-drive/folder/file.txt";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$z = "A:/file.txt";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$z = "schemeName:/path/file.txt";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$v = "http:https"; // Compliant
$x = "my/folder"; // Compliant
$x = "./my/folder"; // Compliant
$x = ".\\my\\folder"; // Compliant
$x = "../my/folder"; // Compliant
$x = "//meta"; // Compliant
$cFile = "c:\\blah\\blah\\blah.txt" ; // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$cFile = "~\\blah\\blah\\blah.txt" ; // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$cFile = "\\\\blah\\blah\\";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
$cFile = "\\d";  // Compliant (regex)
$inputFilePath = $p."//".$n ;// Noncompliant {{Remove this hard-coded path-delimiter.}}
foo($p."//".$n);
$inputFilePath = $p."//". $n;  // Noncompliant {{Remove this hard-coded path-delimiter.}}
//
$z = "schemeName:/path/\(filename)";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
preg_match('/^O\:\d+\:/', $process->getOutput());
$v_path .= '/';
