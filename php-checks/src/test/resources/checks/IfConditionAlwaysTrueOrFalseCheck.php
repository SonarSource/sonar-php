<?php

if (a) {              // OK
}

if (null) {           // OK
}

if (a === b) {        // OK
  doSomething();
}

  if (true) {           // Noncompliant {{Remove this "if" statement.}}
//^^^^^^^^^
  doSomething();
}

if (false) {          // Noncompliant
  doSomethingElse();
}
