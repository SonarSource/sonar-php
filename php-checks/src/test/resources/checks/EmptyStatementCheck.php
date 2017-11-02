<?php
declare(ticks=1);                              // OK

function f() {
  doSomething();                               // OK
  doSomethingElse();;                          // Noncompliant {{Remove this empty statement.}}
//                  ^
  ;                                            // Noncompliant
}

for ($i = 1; $i <= 10; doSomething(), $i++);   // Noncompliant


