<?php

function sayHello() {

  if (true) doSomething();                // Noncompliant {{Add curly braces around the nested statement(s).}}
//^^

  for (i = 0; i < 10; i++) doSomething(); // Noncompliant {{Add curly braces around the nested statement(s).}}
//^^^

  while (true) doSomething();             // Noncompliant {{Add curly braces around the nested statement(s).}}
//^^^^^

  do something(); while (condition);      // Noncompliant {{Add curly braces around the nested statement(s).}}

  if (true)                               // Noncompliant {{Add curly braces around the nested statement(s).}}
    if (true) {
    }

  if (true) {
  } else if (true)                        // Noncompliant {{Add curly braces around the nested statement(s).}}
      doSomething();

  if (true) {                             // OK
  } else doSomething();                   // Noncompliant {{Add curly braces around the nested statement(s).}}

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

  if (true) :                             // OK
    doSomething();
  elseif (true) :                         // OK
    doSomething();
  else :                                  // OK
    doSomething();
  endif;

  for (i = 0; i < 10; i++) :              // OK
    doSomething();
  endfor;

  while (true) :                          // OK
    doSomething();
  endwhile;

  foreach ($a as $b) {                    // OK
  }

  foreach ($a as $b)                      // Noncompliant {{Add curly braces around the nested statement(s).}}
    doSomething();

  foreach ($a as $b) :                    // OK
    doSomething();
  endforeach;

}
