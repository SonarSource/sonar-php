<?php

class SomeException extends Exception {}

new SomeClass();    // OK
SomeFunction();    // OK
SomeException();    // OK
SomeException;    // OK

  new SomeException();    // Noncompliant {{Throw this exception or remove this useless statement}}
//^^^^^^^^^^^^^^^^^^^
  new SomeException; // Noncompliant {{Throw this exception or remove this useless statement}}
//^^^^^^^^^^^^^^^^^

throw new SomeException(); // OK

$exception = new SomeException(); // OK

$util->setLogger(new class {
    public function log($msg)
    {
      new SomeException; // Noncompliant {{Throw this exception or remove this useless statement}}
    //^^^^^^^^^^^^^^^^^
    }
});

class MyError extends Exception {}
new MyError(); // Noncompliant
throw new MyError(); // OK

class MyOtherError extends exception {}
new MyOtherError(); // Noncompliant

class NotReallyAnException {}
new NotReallyAnException(); // OK

class MyRuntimeError extends RuntimeException {}
new MyRuntimeError(); // Noncompliant
