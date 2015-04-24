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

          foreach ($arr as $v) {    // level 5 - NOK
          }

          while (false) {           // level 5 - NOK
          }

          do {                      // level 5 - NOK
          } while (false);

          switch ($a) {             // level 5 - NOK
          }

          try {                     // level 5 - NOK
          } catch (Exception $e) {
            if ($a) {               // level 6 - NOK
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
       }
    }
  }
}
