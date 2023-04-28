<?php
// basic cases with each construct
echo "string";
  echo("string");               // Noncompliant {{Remove the parentheses from this "echo" call.}}
//^^^^^^^^^^^^^^
break 1;
break(1);                       // Noncompliant {{Remove the parentheses from this "break" call.}}
$b = clone $a;
$b = clone($a);                 // Noncompliant {{Remove the parentheses from this "clone" call.}}
switch(1) {
    case 1: break;
    case(2): break;             // Noncompliant {{Remove the parentheses from this "case" call.}}
//  ^^^^^^^^
}
continue 1;
continue(1);                    // Noncompliant {{Remove the parentheses from this "continue" call.}}
include "file.php";
include("file.php");            // Noncompliant {{Remove the parentheses from this "include" call.}}
include_once "file.php";
include_once("file.php");       // Noncompliant {{Remove the parentheses from this "include_once" call.}}
require "file.php";
require("file.php");            // Noncompliant {{Remove the parentheses from this "require" call.}}
require_once "file.php";
require_once("file.php");       // Noncompliant {{Remove the parentheses from this "require_once" call.}}
print "string";
print("string");                // Noncompliant {{Remove the parentheses from this "print" call.}}
return 1;
return(1);                      // Noncompliant {{Remove the parentheses from this "return" call.}}
return(1+1)*2;
throw new Exception("string");
throw(new Exception("string")); // Noncompliant {{Remove the parentheses from this "throw" call.}}
yield 1;
yield(1);                       // Noncompliant {{Remove the parentheses from this "yield" call.}}
yield from [1, 2, 3];
yield from([1, 2, 3]);          // Noncompliant {{Remove the parentheses from this "yield from" call.}}

// more advanced cases
yield 1 => 2;
  yield(1) => 2;   // Noncompliant
//^^^^^^^^^^^^^
yield 1 => (2);
yield(1) => (2); // Noncompliant
echo ("string"), "string";
echo ($a != $b ? "str1" : "str2")."rest";

// case with binary expression
print("string") && false; // Noncompliant

if ( print  "string"            ) {}
if ( print ("string")           ) {} // Noncompliant
if ( print  "string"  && false  ) {}
if ( print ("string") && false  ) {} // Noncompliant
//   ^^^^^^^^^^^^^^^^^^^^^^^^^
if ( print ("string") == false  ) {} // Noncompliant
if ( print ("string") .  "str"  ) {}
if ( print ("string"  .  "str") ) {} // Noncompliant
if ( print~("string")           ) {}
if ( print (~"string")          ) {} // Noncompliant

if ( print ("string") || false || true ) {}          // Noncompliant
if ( print ("string") || false && true ) {}          // Noncompliant
if ( print ("string") ^ false xor true && true ) {}  // Noncompliant

// Binary expression: operations are compliant while comparison are not
print("string") . "";
print("string") + 1;
print("string") - 1;
print("string") * 1;
print("string") / 1;
print("string") % 1;
print("string") ** 1;
print("string") << 1;
print("string") >> 1;
print("string") == 1;  // Noncompliant
print("string") != 1;  // Noncompliant
print("string") <> 1;  // Noncompliant
print("string") === 1; // Noncompliant
print("string") !== 1; // Noncompliant
print("string") <=> 1; // Noncompliant
print("string") > 1;   // Noncompliant
print("string") < 1;   // Noncompliant
print("string") >= 1;  // Noncompliant
print("string") <= 1;  // Noncompliant
print("string") <=> 1; // Noncompliant


// other cases
echof("string");
$var = "echo";
$var("string");
