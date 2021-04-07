<?php

class NoThrowableA {}
//    ^^^^^^^^^^^^> {{Class definition.}}
try {
    foo();
} catch (NoThrowableA $e) { // Noncompliant {{Change this type to be a class deriving from "Throwable".}}
//       ^^^^^^^^^^^^
    echo "foo";
}

interface SomeInterface {}
class NoThrowableB implements SomeInterface {}
//    ^^^^^^^^^^^^>
try {
    foo();
} catch (NoThrowableB $e) { // Noncompliant
//       ^^^^^^^^^^^^
    echo "foo";
}

try {
  foo();
} catch (SomeInterface $e) { // Compliant
  echo "foo";
}

class SomeThrowableA implements Throwable {
    // Throwable methods implementation
}

try {
    foo();
} catch (SomeThrowableA $e) { // Compliant
    echo "foo";
}

try {
    foo();
} catch (SomeThrowableA | NoThrowableA $e) { // Noncompliant
//                        ^^^^^^^^^^^^
    echo "foo";
}

class SomeThrowableB extends Exception {}

try {
    foo();
} catch (SomeThrowableB $e) { // Compliant
    echo "foo";
}
