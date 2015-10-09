<?php

// Global namespace
namespace {

  call_user_method();          // NOK {{Replace this "call_user_method()" call with a call to "call_user_func()".}}
  define_syslog_variables();   // NOK {{Remove this "define_syslog_variables()" call.}}

  if (sql_regcase());          // NOK

  setlocale('LC_ALL', "");     // NOK
  setlocale("LC_ALL", "") ;    // NOK {{Use the "LC_ALL" constant instead of a string literal.}}

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

  call_user_method();             // NOK FIXME (SONARPHP-552) False-Positive
  \call_user_method();            // NOK
}
