<?php

for ($i = 0; i < 42; i++){}  // Noncompliant {{Either remove or fill this block of code.}}
//                       ^^

for ($i = 0; i < 42; i++);   // OK

if ($a == 3)                 // OK
{
  // Do nothing because of X and Y
}

class c {
 use A {}                    // Noncompliant
//     ^^
 use A { /*some comment*/}                    // OK
}

try
{ }                           // Noncompliant
 catch (Error $e)           // OK
{
  // Ignore
} finally { }                  // Noncompliant


switch ($a)
  {   }                         // Noncompliant
//^^^^^


switch ($a)
{   /*Some comment*/   }                      // OK


switch ($a):                 // OK
endswitch;

switch ($a) {                // OK
  case 1:
    break;
  default:
    break;
}

function f()                 // OK
{
  doSomething();
}

function foo() {            // OK
}

class c {                  // OK
}

class d {
  function foo(){         // OK
  }
}
