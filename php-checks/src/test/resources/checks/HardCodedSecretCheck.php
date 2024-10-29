<?php

// Global Constants
const PASSED = "abcdefghijklmnopqrs";   // Compliant nothing to do with secrets

// Noncompliant@+1 {{'SECRET' detected in this expression, review this potentially hard-coded secret.}}
const MY_SECRET = "abcdefghijklmnopqrs";
//                ^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+1 {{'api_key' detected in this expression, review this potentially hard-coded secret.}}
const api_key = 'abcdefghijklmnopqrs';

// Noncompliant@+1
const ABC = "abcdefghijklmnopqrs", AUTH = "abcdefghijklmnopqrs", XYZ = "abcdefghijklmnopqrs";
//                                        ^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+2
const TOKEN_HEREDOC = <<<END
abcdefghijklmnopqrs
END;

//TODO how to detect this?
define("AUTH", "abcdefghijklmnopqrs");                    // Noncompliant

define("credential", "abcdefghijklmnopqrs");              // Noncompliant

define("namespace\level\Token", "abcdefghijklmnopqrs");   // Noncompliant


// Variables declarations
$var_ok = "abcdefghijklmnopqrs"; // Compliant

$oauth = "abcdefghijklmnopqrs";  // Noncompliant

// Variables in function
function do_something(): void
{
  $a = "abcdefghijklmnopqrs";         // Compliant
  $api_app = "abcdefghijklmnopqrs";   // Noncompliant
}

// Class Constants (as Variables in AST)
class ContainsConstrants {
  // Noncompliant@+1
  const Token = "abcdefghijklmnopqrs";
//              ^^^^^^^^^^^^^^^^^^^^^

  const API_SOMETHING_KEY = "abcdefghijklmnopqrs";  // Noncompliant
  const AUTH = "abcdefghijklmnopqrs";               // Noncompliant

  const OK_CONSTANT = "abcdefghijklmnopqrs";        // Compliant
}

