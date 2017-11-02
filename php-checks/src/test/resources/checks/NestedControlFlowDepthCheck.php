<?php

function f() {
  if (true) {                     // level 1
    if (true) {                   // level 2
    }
  } elseif (true) {               // level 1
    if (true) {                   // level 2
    }
  }

  for ($i = 0; i < 0; i++) {      // level 1
    foreach ($arr as $v) {        // level 2
      while (false) {             // level 3
        if (true) {               // level 4

          foreach ($arr as $v) {    // Noncompliant [[secondary=-0,-2,-3,-4,-5]]
//        ^^^^^^^
          }

          while (false) {           // Noncompliant
          }

          do {                      // Noncompliant
          } while (false);

          switch ($a) {             // Noncompliant
          }

          try {                     // Noncompliant {{Refactor this code to not nest more than 4 "if", "for", "while", "switch" and "try" statements.}}
          } catch (Exception $e) {
            if ($a) {               // level 6 - OK
            }
          }
        }
      }
    }
  }

  if (true) {                     // level 1
    if (true) {                   // level 2
      if (true) {                 // level 3
       } else if (true) {         // level 3
        for (;;) {
         }
       } else {
        for (;;) {
         }       
       }
    }
  }
}
