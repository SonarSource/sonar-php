<?php

  echo (expression);                            // Noncompliant {{Remove the parentheses from this "echo" call.}}
//^^^^

echo expression;                              // OK
echo (parenthesis_expression), expression;    // OK

