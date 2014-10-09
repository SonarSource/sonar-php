<?php

!true;            // NOK
!false;           // NOK
a == false;       // NOK
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
a ? b : false;    // NOK
a || true || b || true; // NOK

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
