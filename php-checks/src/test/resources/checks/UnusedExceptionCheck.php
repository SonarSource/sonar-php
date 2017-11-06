<?php

/***/
function f() {
  new SomeClass();    // OK
  someFunction();    // OK
  someException();    // OK
  someException;    // OK
  new someException();    // Noncompliant {{Throw this exception or remove this useless statement}}
//^^^^^^^^^^^^^^^^^^^
  new someException; // Noncompliant {{Throw this exception or remove this useless statement}}
//^^^^^^^^^^^^^^^^^

  new foo::someException(); // Noncompliant {{Throw this exception or remove this useless statement}}
//^^^^^^^^^^^^^^^^^^^^^^^^

$util->setLogger(new class {
    public function log($msg)
    {
      new someException; // Noncompliant {{Throw this exception or remove this useless statement}}
    //^^^^^^^^^^^^^^^^^
    }
});
}
