<?php
//----------------------
// Function Declarations
//----------------------
function testFunction1($foo) {
  $foo = "string"; // Noncompliant {{Introduce a new variable instead of reusing the parameter "$foo".}}
  ++$foo; // Noncompliant
  --$foo; // Noncompliant
  $foo--; // Noncompliant
  $foo++; // Noncompliant
  $a = $foo; // Compliant
  $a = $foo--; // Noncompliant
}


function testFunction2($foo) {
  if ($a < $b) {
    $foo = null; // Noncompliant
  }
}

//----------------------
// Method Declarations
//----------------------
class TestClass {
  public function testMethod1($foo) {
    $foo = "null"; // Noncompliant
  }
}

//----------------------
// Function Expressions
//----------------------
$functionExpression = function($foo) { $foo = null; }; // Noncompliant

functionExpression = function($foo) {
  $fun = function ($bar) {
    $foo = null; // Compliant
  };

  $foo = null; // Noncompliant
};

//----------------------
//        Loops
//----------------------
foreach ($array as $item) {
  $item = null; // Noncompliant
}

function testFunction1($foo) {
  foreach ($array as $item) { }
  $item = null; // Compliant
  ++$foo; // Noncompliant
}
