<?php

// SHOULD BE IGNORED

$var->
{'user_' . $id};                 // OK


{                                // OK
  echo $a;
}

$var->{'user_' . $id};


// --- FUNCTION DECLARATION ----

function f ()
{                                // NOK {{Move this open curly brace to the end of the previous line.}}
}

$a = function () { doSomething;  // NOK {{Move this open curly brace to the end of the previous line.}}
};

function f() {                   // OK
}

// --- CLASS DECLARATION ---

class A {                        // OK
  function foo();
}
class A { }                      // OK

class A implements B {            // OK
  function foo();
}

class A extends B {              // OK
  function foo();
}

class A extends B
{                                // NOK {{Move this open curly brace to the end of the previous line.}}
  function foo();

  function bar()
  {                              // NOK {{Move this open curly brace to the end of the previous line.}}
    foo();
  }
}

class A extends B { function foo();  // NOK {{Move this open curly brace to the end of the previous line.}}
}



// --- TRY-CATCH STATEMENT ----

try {
  foo();
} catch (Type $e) {
  bar();
} finally {
  foobar();
}

try
{                                    // NOK {{Move this open curly brace to the end of the previous line.}}
  foo();
} catch (Type $e) { bar();           // NOK {{Move this open curly brace to the end of the previous line.}}
}
finally
{                                    // NOK {{Move this open curly brace to the end of the previous line.}}
  foobar();
}


// --- IF STATEMENT ----

if (true)
{                                // NOK {{Move this open curly brace to the end of the previous line.}}
} elseif (true)
{                                // NOK {{Move this open curly brace to the end of the previous line.}}
} else { foo();                  // NOK {{Move this open curly brace to the end of the previous line.}}
}

if (true) { doSomething(); }     // OK


if (true) {  ?>                  // OK
html
<?php }

if (true) { ?> html <? }         // OK


// --- LOOPS ----

for (i = 0; i < 10; i++) { doSomething();  // NOK {{Move this open curly brace to the end of the previous line.}}
}

for (i = 0; i < 10; i++) {        // OK
  doSomething();
}

foreach ($a as $b)
{                                // NOK {{Move this open curly brace to the end of the previous line.}}
}

foreach ($a as $b) {             // OK
}

while (true) {                    // OK
}

do {                              // OK
  something();
} while (true);

while (true) { foo();              // NOK {{Move this open curly brace to the end of the previous line.}}
}

do {   something();                // NOK {{Move this open curly brace to the end of the previous line.}}
} while (true);


// --- DECLARE STATEMENT ----

declare (a)
{                                 // NOK {{Move this open curly brace to the end of the previous line.}}
  foo();
}


// --- USE TRAIT DECLARATION ----

class A {
  use SomeTrait, OtherTrait
  {                                  // NOK {{Move this open curly brace to the end of the previous line.}}
  }

  use SomeTrait, OtherTrait
  {                                  // NOK {{Move this open curly brace to the end of the previous line.}}
    Foo as bar;
  }
}

// --- SWITCH STATEMENT ----

switch (a)
{                                   // NOK {{Move this open curly brace to the end of the previous line.}}
  case 1 :
      break;
}

switch (a)
{                                   // NOK {{Move this open curly brace to the end of the previous line.}}
}

switch (a) { ;                      // NOK {{Move this open curly brace to the end of the previous line.}}
  case 1 :
      break;
}

// --- NAMESPACE ----

namespace Some\NS
{                                   // NOK {{Move this open curly brace to the end of the previous line.}}
  foo();
}

namespace
{                                   // NOK {{Move this open curly brace to the end of the previous line.}}
  foo();
}

namespace {                         // OK
}