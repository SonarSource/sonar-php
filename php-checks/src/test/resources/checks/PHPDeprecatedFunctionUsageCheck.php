<?php

// Global namespace
namespace {

  call_user_method();          // Noncompliant {{Replace this "call_user_method()" call with a call to "call_user_func()".}}
//^^^^^^^^^^^^^^^^
  define_syslog_variables();   // Noncompliant {{Remove this "define_syslog_variables()" call.}}

  if (sql_regcase());          // Noncompliant

  setlocale('LC_ALL', "");     // Noncompliant
  setlocale("LC_ALL", "") ;    // Noncompliant {{Use the "LC_ALL" constant instead of a string literal.}}
//          ^^^^^^^^

  __autoload();                // Noncompliant
  create_function('', 'echo 42;'); // Noncompliant
  parse_str($str);             // Noncompliant {{Add a second argument to this call to "parse_str".}}
  parse_str($str, $array);     // OK
  gmp_random(4);               // Noncompliant
  each($foo);                  // Noncompliant
  assert();                    // OK
  assert($foo);                // OK
  assert("$foo");              // Noncompliant {{Change this call to "assert" to not pass a string argument.}}
  assert('foo()');             // Noncompliant

  \A\call_user_method();       // OK

  call_user_func();            // OK
  sql_regcase->func();         // OK
  setlocale(LC_ALL, "");       // OK
  setlocale("0", "") ;         // OK
  setlocale();                 // OK

}

namespace A {
  function call_user_method() {
  }

  call_user_method();             /* Noncompliant */ // FIXME (SONARPHP-552) False-Positive
  \call_user_method();            // Noncompliant
}
