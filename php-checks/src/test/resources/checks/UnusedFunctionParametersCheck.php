<?php

function f($p1, $p2, $p3) {              // Noncompliant {{Remove the unused function parameter "$p1".}}
//         ^^^
    $p2 = 1;
    call($p3);
}

$a = function($p1, $p2) { return $p1; }; // Noncompliant {{Remove the unused function parameter "$p2".}}


function f($p1, $p2) {                   // Noncompliant {{Remove the unused function parameter "$p1".}}
  function nestedF($p1, $p2) {           // Noncompliant {{Remove the unused function parameter "$p2".}}
    $p1 = 1;
  }
  return $p2;
}

class C {

// Noncompliant@+1 {{Remove the unused function parameter "$p2".}}
  public function f1($p1, $p2, $p3) {    // Noncompliant {{Remove the unused function parameter "$p3".}}
      return $p1;
  }

  /*
   * @inheritdoc
   */
  public function f2($p1) {              // OK
    return 1;
  }

  public function f3($p1);               // OK
}

function f($p1, $p2) {                   // OK
  $p1 = $p2;
}

class D extends A {
  public function f1($p1) {} // OK
  private function f2($p1) {}               // Noncompliant {{Remove the unused function parameter "$p1".}}
}

class E implements B {

  public function f1($p1) {} // OK

  public function f2() {
    $f = function($p1) {};    // Noncompliant
  }
}

class K {
  public function f1($p1, $p2, $p3, $var) {    // OK
      doSomething("${var}");
      return "$p1 ${p2} {$p3}";
  }
}

function foo($p1) {   // OK
   f1 = function() use ($p1) {
     echo $p1;
   };
}


function foo($variable) {  // OK
    $array = compact('variable');
    return $array;
}

function bar($p) {
    $vars = 'p';
    return compact($vars);
}

function bar2($p) { // Noncompliant
    $vars = 'p';
    return compact('a');
}


class Foo {
  public function f1($p1) {                   // Noncompliant {{Remove the unused function parameter "$p1".}}
    $x = new class extends A {
     public function f1($p1) {}                 // OK
     private function f2($p1) {}               // Noncompliant {{Remove the unused function parameter "$p1".}}
    };
 }
}
