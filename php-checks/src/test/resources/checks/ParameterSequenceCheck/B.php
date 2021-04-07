<?php

someFunction($z, $y, $x); // Noncompliant
someOtherFunction($a, $b); // Compliant

class SomeOtherClass {
  public function bar($x, $y, $z) {}

  public function overriddenMethod($a, $b) {}
}
