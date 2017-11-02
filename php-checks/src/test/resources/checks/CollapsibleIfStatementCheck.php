<?php

class A {
  function f() {
    if (false) {        // OK
    }

    if (false) {        // OK
    } else {
    }

    if (false) {        // OK
      if (false) {      // Noncompliant {{Merge this if statement with the enclosing one.}}
//    ^^
      }
    }

    if (false) {        // OK
      if (false) {      // OK
      }
      doSomething();
    }

    if (false) {        // OK
      $a = 1;
      if (a) {          // OK
      }
    }

    if (false) {        // OK
      if (false) {      // OK
      }
    } else {
    }

    if (false) {        // OK
      if (false) {      // OK
      } else {
      }
    }

    if (false) {        // OK
    } else if (false) { // OK
      if (false) {      // Noncompliant
      }
    }

    if (false)          // OK
      if (true) {       // Noncompliant
      }

    if (true)           // OK
     doSomething();

    if (false) {        // OK
      while (true) {
        if (true) {     // OK
        }
      }
    }

    while (true)
      if(true) {        // OK
      }

    if (false) {        // OK
    }

    if (false) {        // OK
      doSomething();
    }

    if (true):
      if (false):       // Noncompliant
        doSomething();
      endif;
    endif;

    if (false) {        
      if (false) {      // OK
      } elseif (false) {
      }
    }

    if (false) {
      if (false) {       // OK
      }
     } elseif (true) {

     }

    if (true) {         // OK
      halt();
    }
  }
}
