<?php

/**
 * Space around arguments' comma
 */

function f($p1 , $p2, $p3, $p4) {}  // Noncompliant {{Remove any space before comma separated arguments.}}
//             ^
function g($p1,$p2, $p3, $p4) {}    // Noncompliant {{Put exactly one space after comma separated arguments.}}
//            ^
function h($p1 ,$p2, $p3, $p4) {}   // Noncompliant {{Remove any space before comma separated arguments and put exactly one space after comma separated arguments.}}
//             ^
function i($p1 ,$p2, $p3 ,$p4) {}   // Noncompliant {{Remove any space before comma separated arguments and put exactly one space after comma separated arguments.}}
//             ^
doSomething($p1,$p2);               // Noncompliant
//             ^
function j($p1, $p2, $p3, $p4) {}   // OK

/**
 * No space after function's name
 */

function f () {}     // Noncompliant {{Remove all space between the method name "f" and the opening parenthesis.}}
//       ^
$a->doSomething ();  // Noncompliant {{Remove all space between the method name "doSomething" and the opening parenthesis.}}
//  ^^^^^^^^^^^
function g() {}      // OK
doSomething();       // OK

/**
 * Closure spacing
 */

$f = function() {};           // Noncompliant {{Put exactly one space between the "function" keyword and the opening parenthesis.}}
//   ^^^^^^^^
$g = function () use($a) {};  // Noncompliant {{Put exactly one space before and after the "use" keyword.}}
//               ^^^
$h = function () use ($b) {}; // OK

foo->
  bar($a,$b);    // Noncompliant
//      ^

echo "la" , "lala";    // OK - internal
Echo "la" , "lala";    // OK - internal
