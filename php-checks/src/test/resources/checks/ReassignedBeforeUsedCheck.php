<?php

//----------------------
// Function Declarations
//----------------------

function foo($d) {
  $d = 42; // Noncompliant {{Introduce a new variable instead of reusing the parameter "$d".}}
  bar($d);
}

function foo($d) {
  $d = 42; // Noncompliant
}

function foo(&$d) {
  $d = 42; // Compliant
}

function foo(&$d) {
  $d = 42; // Compliant
  bar($d);
}

function foo($p) {
  bar($p);
  $p = 42; // Compliant
}

function foo($p) {
  if (bar()) {
    $p = 42; // Compliant
  }
  echo $p;
}

function foo($p) {
  echo $p; // Compliant
  $p = 42;
  echo $p;
}

function foo($p) {
  if (bar()) {
    $p = 42; // Noncompliant
  } else {
    $p = 43;
  }
  echo $p;
}

function foo($p) {
  if (bar()) {
    $p = 42; // Compliant
  } else {
    foo();
  }
  echo $p;
}

function foo($p) {
  if (bar()) {
    if (bar()) {
      $p = 42; // Compliant
    } else {
      foo();
    }
  } else {
    $p = 43; // Compliant
  }
  echo $p;
}

function foo($g) {
  if (bar()) {
    $g = 42; // Noncompliant
    echo $g;
  }
}

function testFunction1($foo) {
//                     ^^^^>
  foreach ($array as $item) {
    $foo = $item; // Noncompliant
//  ^^^^^^^^^^^^
  }
}

//----------------------
// Method Declarations
//----------------------
class TestClass {
  public function foo($foo) {
    $foo = 42; // Noncompliant
    $bar = $foo;
  }
  public function foo($foo) {
    $bar = $foo;
    $foo = 42; // Compliant
  }
}

//----------------------
// Function Expressions
//----------------------
$functionExpression = function($foo) {
  $foo = null; // Noncompliant
  $bar = $foo;
};

$functionExpression = function($foo) {
  echo $foo;
  $foo = null; // Compliant
};

functionExpression = function($foo) {
  $fun = function ($bar) {
    $foo = null; // Compliant
  };

  $fun = function ($bar) use ($foo) {
    $foo = null; // False Negative
  };

  $foo = null; // Compliant
  echo $foo;
};

//----------------------
//      Coverage
//----------------------
$globals = $engine->getGlobals();

function foo($p) {
  break;
  $p = 42;
}
