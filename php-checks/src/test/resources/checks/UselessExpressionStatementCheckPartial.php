<?php
  return method_call('string')."concat";
  method_call('string')."concat";   // Noncompliant {{This statement part is useless, remove or refactor it.}}
//                     ^^^^^^^^^
  "concat".method_call('string');   // Noncompliant {{This statement part is useless, remove or refactor it.}}
//^^^^^^^^^
  method1().method2();   // Noncompliant {{This binary operation is useless, remove it.}}
//         ^
  method_call('string')."concat"."other"; // Noncompliant {{This statement part is useless, remove or refactor it.}}
//                              ^^^^^^^^
  "concat".method_call('string')."other"; // Noncompliant {{This statement part is useless, remove or refactor it.}}
//                              ^^^^^^^^
  "concat"."other".method_call('string'); // Noncompliant {{This statement part is useless, remove or refactor it.}}
//^^^^^^^^^^^^^^^^^
  method1()."other".method2(); // Noncompliant {{This binary operation is useless, remove it.}}
//                 ^
  "concat".(method_call('string')."other"); // Noncompliant {{Remove or refactor this statement.}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
?>
