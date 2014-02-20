<?php

function sayHello() {

  if (true) doSomething();                // NOK

  for (i = 0; i < 10; i++) doSomething(); // NOK

  while (true) doSomething();             // NOK

  do something(); while (condition);      // NOK

  if (true)                               // NOK
    if (true) {
    }

  if (true) {
  } else if (true)                        // NOK
      doSomething();

  if (true) {                             // OK
  } else doSomething();                   // NOK

  if (condition) {                        // OK
  }

  if (true) {                             // OK
  } elseif (condition) {                  // OK
  }

  for (i = 0; i < 10; i++) {              // OK
  }

  while (true) {                          // OK
  }

  do {                                    // OK
    something();
  } while (true);

  if (true) {
  } else if (true) {                      // OK
  }

  if (true);                              // OK
}
