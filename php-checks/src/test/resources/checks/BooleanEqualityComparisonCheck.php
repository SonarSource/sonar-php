<?php

!true;            // NOK {{Remove the literal "true" boolean value.}}
!false;           // NOK {{Remove the literal "false" boolean value.}}
a == false;       // NOK
//   ^^^^^
a == true;        // NOK
a != false;       // NOK
a != true;        // NOK
false == a;       // NOK
true == a;        // NOK
false != a;       // NOK
true != a;        // NOK
false && foo();   // NOK
foo() || true;    // NOK
a == true == b;   // NOK
a ? true : b;     // NOK
a ? : true;       // NOK
a ? b : false;    // NOK

a || true         // NOK
  || b
  || true;        // NOK

a === false;      // OK - exception
a === true;       // OK - exception
a !== false;      // OK - exception
a !== true;       // OK - exception
a == foo(true);   // OK
true < 0;         // OK
~true;            // OK
++ true;          // OK
!foo;             // OK
foo() && bar();   // OK
