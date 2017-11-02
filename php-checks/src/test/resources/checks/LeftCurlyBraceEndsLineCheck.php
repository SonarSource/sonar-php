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
  {                                // Noncompliant {{Move this open curly brace to the end of the previous line.}}
//^
}

$a = function () { doSomething;  // Noncompliant {{Move this open curly brace to the end of the previous line.}}
//               ^
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
{                                // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  function foo();

  function bar()
  {                              // Noncompliant {{Move this open curly brace to the end of the previous line.}}
    foo();
  }
}

class A extends B { function foo();  // Noncompliant {{Move this open curly brace to the end of the previous line.}}
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
{                                    // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  foo();
} catch (Type $e) { bar();           // Noncompliant {{Move this open curly brace to the end of the previous line.}}
}
finally
{                                    // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  foobar();
}


// --- IF STATEMENT ----

if (true)
{                                // Noncompliant {{Move this open curly brace to the end of the previous line.}}
} elseif (true)
{                                // Noncompliant {{Move this open curly brace to the end of the previous line.}}
} else { foo();                  // Noncompliant {{Move this open curly brace to the end of the previous line.}}
}

if (true) { doSomething(); }     // OK


if (true) {  ?>                  // OK
html
<?php }

if (true) { ?> html <? }         // OK


// --- LOOPS ----

for (i = 0; i < 10; i++) { doSomething();  // Noncompliant {{Move this open curly brace to the end of the previous line.}}
}

for (i = 0; i < 10; i++) {        // OK
  doSomething();
}

foreach ($a as $b)
{                                // Noncompliant {{Move this open curly brace to the end of the previous line.}}
}

foreach ($a as $b) {             // OK
}

while (true) {                    // OK
}

do {                              // OK
  something();
} while (true);

while (true) { foo();              // Noncompliant {{Move this open curly brace to the end of the previous line.}}
}

do {   something();                // Noncompliant {{Move this open curly brace to the end of the previous line.}}
} while (true);


// --- DECLARE STATEMENT ----

declare (a)
{                                 // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  foo();
}


// --- USE TRAIT DECLARATION ----

class A {
  use SomeTrait, OtherTrait
  {                                  // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  }

  use SomeTrait, OtherTrait
  {                                  // Noncompliant {{Move this open curly brace to the end of the previous line.}}
    Foo as bar;
  }
}

// --- SWITCH STATEMENT ----

switch (a)
{                                   // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  case 1 :
      break;
}

switch (a)
{                                   // Noncompliant {{Move this open curly brace to the end of the previous line.}}
}

switch (a) { ;                      // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  case 1 :
      break;
}

// --- NAMESPACE ----

namespace Some\NS
{                                   // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  foo();
}

namespace
{                                   // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  foo();
}

namespace {                         // OK
}


// --- ANONYMOUS CLASS  ---

$x = new class {                        // OK
  function foo();
};
$x = new class { };                      // OK

$x = new class implements B {            // OK
  function foo();
};

$x = new class extends B {              // OK
  function foo();
};

$x = new class extends B
{                                // Noncompliant {{Move this open curly brace to the end of the previous line.}}
  function foo();

  function bar()
  {                              // Noncompliant {{Move this open curly brace to the end of the previous line.}}
    foo();
  }
};

$x = new class extends B { function foo();  // Noncompliant {{Move this open curly brace to the end of the previous line.}}
};
