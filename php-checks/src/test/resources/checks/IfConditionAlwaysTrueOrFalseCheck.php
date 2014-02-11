<?php

if (a === b) {        // OK
  doSomething();
}

if (true) {
  doSomething();      // NOK
}

if (false) {
  doSomethingElse();  // NOK
}
