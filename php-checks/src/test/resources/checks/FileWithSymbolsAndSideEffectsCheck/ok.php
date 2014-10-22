<?php

define(NAME, "value");   // OK

if (true) {              // OK
  function f() {}
}

class A {

  public function f() {
    print "Hello World!";  // OK - within declaration
  }

}
