<?php

try {
} catch (Exception $e) {
  throw new Exception("foo", 0, $e);  // Compliant - using $e
}

try {
} catch (Exception $e) {
  throw new Exception($e->getMessage());  // Compliant - using $e
}

try {
} catch (Exception $e) {
  throw $e;  // Noncompliant {{Add logic to this catch clause or eliminate it and rethrow the exception automatically.}}
//^^^^^^^^^
} finally {
  doSomething();
}

try {
} catch (Exception $e) {
  doSomething();  // Compliant - not throwing
}

try {
} catch (Exception1 $e) {
  throw $e;  // Compliant - multiple catches, one is not simply retrowing
} catch (Exception2 $e) {
  throw new Exception($e->getMessage());
}

try {
} catch (Exception1 $e) {
  throw $e;  // Noncompliant
} catch (Exception2 $e) {
  throw $e;  // Noncompliant
}

try {
} catch (Exception $e) {
  doSomething();
  throw $e;  // Compliant
}
