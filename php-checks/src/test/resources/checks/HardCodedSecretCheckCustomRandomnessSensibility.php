<?php

// Global Constants
const PASSED = "abcdefghijklmnopqrs";   // Compliant nothing to do with secrets

// Noncompliant@+1 {{'SECRET' detected in this expression, review this potentially hard-coded secret.}}
const MY_SECRET = "Zk4mPq8Vr2Wn5TySbQr9FxLm";
//                ^^^^^^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+1 {{'api_key' detected in this expression, review this potentially hard-coded secret.}}
const api_key = 'Zk4mPq8Vr2Wn5TySbQr9FxLmYc';

// Noncompliant@+1
const ABC = "abcdefghijklmnopqrs", AUTH = "Zk4mPq8Vr2Wn5TySbQr9FxLmYc", XYZ = "abcdefghijklmnopqrs";
//                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^

// Compliant, due custom randomnessSensibility it is not random
define("AUTH", "abcdefghijklmnopqrs");

define("credential", "Zk4mPq8Vr2Wn5TySbQr9FxLm");              // Noncompliant

define("namespace\level\Token", "Zk4mPq8Vr2Wn5TySbQr9FxLm");   // Noncompliant

$var_ok = "Zk4mPq8Vr2Wn5TySbQr9FxLmYc"; // Compliant

$oauth = "Zk4mPq8Vr2Wn5TySbQr9FxLmYc";  // Noncompliant

function do_something(): void
{
  $a = "Zk4mPq8Vr2Wn5TySbQr9FxLmYc";         // Compliant
  $apikey = "Zk4mPq8Vr2Wn5TySbQr9FxLmYc";    // Noncompliant
}
