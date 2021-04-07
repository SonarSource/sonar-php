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

  !(foo() <= bar()); // Noncompliant
//^^^^^^^^^^^^^^^^^

(foo() <= bar()) && !($a === foo()); // Noncompliant
//                  ^^^^^^^^^^^^^^^

// Other valid UnaryExpression related to booleans
$c = -($a >= $b); // Compliant
$c = +($a >= $b); // Compliant
$c = @($a >= $b); // Compliant

if (substr($string, 0, 3) !== '+OK') {}  // Compliant
if (!$this->connected) {}  // Compliant
