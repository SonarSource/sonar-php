<?php

// Global Constants
const PASSED = "abcdefghijklmnopqrs";   // Compliant nothing to do with secrets

// Noncompliant@+1 {{'SECRET' detected in this expression, review this potentially hard-coded secret.}}
const MY_SECRET = "abcdefghijklmnopqrstuvwx";
//                ^^^^^^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+1 {{'api_key' detected in this expression, review this potentially hard-coded secret.}}
const api_key = 'abcdefghijklmnopqrstuvwxyz';

// Noncompliant@+1
const ABC = "abcdefghijklmnopqrs", AUTH = "abcdefghijklmnopqrstuvwxyz", XYZ = "abcdefghijklmnopqrs";
//                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^

// Compliant, due custom randomnessSensibility it is not random
define("AUTH", "abcdefghijklmnopqrs");

define("credential", "abcdefghijklmnopqrstuvwx");              // Noncompliant

define("namespace\level\Token", "abcdefghijklmnopqrstuvwx");   // Noncompliant

$var_ok = "abcdefghijklmnopqrstuvwxyz"; // Compliant

$oauth = "abcdefghijklmnopqrstuvwxyz";  // Noncompliant

function do_something(): void
{
  $a = "abcdefghijklmnopqrstuvwxyz";         // Compliant
  $apikey = "abcdefghijklmnopqrstuvwxyz";    // Noncompliant
}
