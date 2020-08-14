<?php

function someFunction($x, $y, $z) {}

function someOtherFunction($a, $b) {}

class someClass extends someOtherClass {
  public function foo() {
    $this->bar($y, $x, $z); // Noncompliant
    $this->overriddenMethod($b, $a); // Compliant
  }

  public function overriddenMethod($b, $a) {}
}
