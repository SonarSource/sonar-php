<?php

/**
 * Space around arguments' comma
 */

function f($p1 , $p2, $p3, $p4) {}  // NOK - space before
function g($p1,$p2, $p3, $p4) {}    // NOK - no space after
function h($p1 ,$p2, $p3, $p4) {}   // NOK - space after & no space after
function i($p1 ,$p2, $p3 ,$p4) {}   // NOK - space after & no space after x2
doSomething($p1,$p2);               // NOK
function j($p1, $p2, $p3, $p4) {}   // OK

/**
 * No space after function's name
 */

function f () {}     // NOK
$a->doSomething ();  // NOK
function g() {}      // OK
doSomething();       // OK

/**
 * Closure spacing
 */

$f = function() {};           // NOK
$g = function () use($a) {};  // NOK
$h = function () use ($b) {}; // OK
