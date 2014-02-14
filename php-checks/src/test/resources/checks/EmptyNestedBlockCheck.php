<?php

for ($i = 0; i < 42; i++){}  // NOK

for ($i = 0; i < 42; i++);   // OK

if ($a == 3)                 // OK
{
  // Do nothing because of X and Y
}

class c {
 use A {}                    // NOK
}

try                          // NOK
{
} catch (Error $e)           // OK
{
  // Ignore
} finally {
}

switch ($a)                  // NOK
{
}

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

class c {}                   // OK
