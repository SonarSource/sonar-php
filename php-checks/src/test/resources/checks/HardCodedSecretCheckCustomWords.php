<?php

// Global Constants
const PASSED = "Qm7vXpLr2FzT9baWtHx";   // Compliant nothing to do with secrets

// Noncompliant@+1 {{'CUSTOM' detected in this expression, review this potentially hard-coded secret.}}
const CUSTOM = "Qm7vXpLr2FzT9baWtHx";
//             ^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+1 {{'custom' detected in this expression, review this potentially hard-coded secret.}}
const customer = 'Qm7vXpLr2FzT9baWtHx';

// Noncompliant@+1
const ABC = "Qm7vXpLr2FzT9baWtHx", APP = "Qm7vXpLr2FzT9baWtHx", XYZ = "Qm7vXpLr2FzT9baWtHx";
//                                       ^^^^^^^^^^^^^^^^^^^^^

define("AUTH", "Qm7vXpLr2FzT9baWtHx");          // Compliant, no 'custom' or 'app' in name

$oauth = "Qm7vXpLr2FzT9baWtHx";                 // Compliant, no 'custom' or 'app' in name

// Variables in function
function do_something(): void
{
  $a = "Qm7vXpLr2FzT9baWtHx";                   // Compliant, no 'custom' or 'app' in name
  $apikey = "Qm7vXpLr2FzT9baWtHx";              // Compliant, no 'custom' or 'app' in name
  $my_application = "Qm7vXpLr2FzT9baWtHx";      // Noncompliant
}
