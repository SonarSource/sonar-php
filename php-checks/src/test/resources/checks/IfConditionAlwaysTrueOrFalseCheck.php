<?php

if (a) {              // OK
}

if (null) {           // OK
}

if (a === b) {        // OK
  doSomething();
}

if (true) {           // NOK {{Remove this "if" statement.}}
  doSomething();
}

if (false) {          // NOK
  doSomethingElse();
}
