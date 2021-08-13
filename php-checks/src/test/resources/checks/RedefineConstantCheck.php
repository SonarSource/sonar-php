<?php

  define("FOO", 1);
//^^^^^^^^^^^^^^^^> {{Initial constant definition.}}
  define("FOO", 2); // Noncompliant {{Make sure to not redefine a constant.}}
//^^^^^^^^^^^^^^^^

  if ($a == 1) {
    define("FOO", 3); // FN
    define("BAR", 1);
  } elseif ($a == 2) {
    define("BAR", 2);
  } else {
    define("BAR", 3);
  }
  define("BAR", 4); // FN

  $a > 5 ? define("FOO", 5) : define("FOO", 6);

  function foo() {
    define("FOO", 1);
    define("BAR", 1);
    define("BAR", 2); // Noncompliant
  }

  const FOO = 1; // FN

  class Foo {

    public function bar() {
      define("FOO", 1);
      define("FOO", 2); // Noncompliant
    }
  }

  interface Bar {
    public function foo();
  }

  foo();
  define($foo, "1");

