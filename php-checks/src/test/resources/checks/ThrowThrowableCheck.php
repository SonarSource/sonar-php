<?php

class NoThrowable {}
//    ^^^^^^^^^^^> {{Class definition.}}
class SomeThrowable implements Throwable { /* Throwable methods implementation */ }
class SomeCustomException extends Exception {};

  throw new NoThrowable(); // Noncompliant {{Throw an object derived from "Throwable".}}
//^^^^^^^^^^^^^^^^^^^^^^^
throw new NoThrowable; // Noncompliant

throw new Exception(); // Compliant
throw new SomeCustomException(); // Compliant
throw new SomeThrowable(); // Compliant

throw new $x(); // Compliant
throw new $x; // Compliant
throw $foo; // Compliant
$x || throw new SomeThrowable();
$x || throw new NoThrowable(); // Noncompliant
