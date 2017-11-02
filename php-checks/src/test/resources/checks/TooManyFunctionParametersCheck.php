<?php

function f($p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8) { // Noncompliant {{This function has 8 parameters, which is greater than the 2 authorized.}}
//        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

function g($p1, $p2, $p3) { // Noncompliant
}

function h($p1, $p2) {
}

class A {
  function __construct($p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8) { // Noncompliant
  }  
}

class B {
  function __construct($p1, $p2, $p3, $p4, $p5, $p6, $p7) {
  }
}

class C {
  function f($p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8) { // Noncompliant
  }
}

$x = new class() {
  function __construct($p1, $p2, $p3, $p4, $p5, $p6, $p7) {
  }
};
