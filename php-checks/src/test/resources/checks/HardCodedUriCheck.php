<?php
$filepath = "http://www.mywebsite.com";  // Noncompliant {{Refactor your code to get this URI from a customizable parameter.}}
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^
$filepath = $someVar;
$y = "ftp://myserver.com";

$z = "~/a";
$z = "/Folder/";
$z = "/folder/file.txt";
$z = "//my-network-drive/file.txt";
$z = "//my-network-drive/folder/file.txt";
$z = "A:/file.txt";
$z = "schemeName:/path/file.txt";
$v = "http:https"; // Compliant
$x = "my/folder"; // Compliant
$x = "./my/folder"; // Compliant
$x = ".\\my\\folder"; // Compliant
$x = "../my/folder"; // Compliant
$x = "//meta"; // Compliant
$cFile = "c:\\blah\\blah\\blah.txt" ;
$cFile = "~\\blah\\blah\\blah.txt" ;
$cFile = "\\\\blah\\blah\\";
$cFile = "\\d";  // Compliant (regex)
$inputFilePath = $p."//".$n ;
foo($p."//".$n);
$inputFilePath = $p."//". $n;
$z = "schemeName:/path/\(filename)";
preg_match('/^O\:\d+\:/', $process->getOutput());
$v_path .= '/';

$x = readfile("http://www.mywebsite.com"); // Noncompliant
$x = readfile("php://www.mywebsite.com"); // compliant
$x = readfile("php://www.mywebsite.com"); // compliant
$x = readfile("php://www.$mywebsite.com"); // compliant
$x = http_build_query("php://www.$mywebsite.com"); // compliant
class A {
 private $filepath = "http://www.mywebsite.com"; // Noncompliant
 private $someVariable = "http://www.mywebsite.com"; // compliant
}
class B {
 private static $filepath;
}
B::$filepath = 12;
