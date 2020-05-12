<?php

if (!($a > $b)) {} // Noncompliant {{Use the opposite operator '<=' instead and remove complement operator.}}
//  ^^^^^^^^^^
if (!($a < $b)) {} // Noncompliant {{Use the opposite operator '>=' instead and remove complement operator.}}
if (!($a <= $b)) {} // Noncompliant {{Use the opposite operator '>' instead and remove complement operator.}}
if (!($a >= $b)) {} // Noncompliant {{Use the opposite operator '<' instead and remove complement operator.}}
if (!($a == $b)) {} // Noncompliant {{Use the opposite operator '!=' instead and remove complement operator.}}
if (!($a === $b)) {} // Noncompliant {{Use the opposite operator '!==' instead and remove complement operator.}}
if (!($a != $b)) {} // Noncompliant {{Use the opposite operator '==' instead and remove complement operator.}}
if (!($a !== $b)) {} // Noncompliant {{Use the opposite operator '===' instead and remove complement operator.}}
$c = !(($a === $b)); // Noncompliant {{Use the opposite operator '!==' instead and remove complement operator.}}
//   ^^^^^^^^^^^^^^

$c = !($a ?? $b); // Compliant
$c = !($a && $b); // Compliant
$c = !($a || $b); // Compliant

  !(foo() <= bar()); // Noncompliant {{Use the opposite operator '>' instead and remove complement operator.}}
//^^^^^^^^^^^^^^^^^

(foo() <= bar()) && !($a === foo()); // Noncompliant {{Use the opposite operator '!==' instead and remove complement operator.}}
//                  ^^^^^^^^^^^^^^^
