<?php

// Global Constants
const PASSED = "abcdefghijklmnopqrs";   // Compliant nothing to do with secrets

// Noncompliant@+1 {{'CUSTOM' detected in this expression, review this potentially hard-coded secret.}}
const CUSTOM = "abcdefghijklmnopqrs";
//             ^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+1 {{'custom' detected in this expression, review this potentially hard-coded secret.}}
const customer = 'abcdefghijklmnopqrs';

// Noncompliant@+1
const ABC = "abcdefghijklmnopqrs", APP = "abcdefghijklmnopqrs", XYZ = "abcdefghijklmnopqrs";
//                                       ^^^^^^^^^^^^^^^^^^^^^

define("AUTH", "abcdefghijklmnopqrs");          // Compliant, no 'custom' or 'app' in name

$oauth = "abcdefghijklmnopqrs";                 // Compliant, no 'custom' or 'app' in name

// Variables in function
function do_something(): void
{
  $a = "abcdefghijklmnopqrs";                   // Compliant, no 'custom' or 'app' in name
  $apikey = "abcdefghijklmnopqrs";              // Compliant, no 'custom' or 'app' in name
  $my_application = "abcdefghijklmnopqrs";      // Noncompliant
}
