<?php

  class A { // Noncompliant {{Class "A" has 6 lines, which is greater than the 5 authorized. Split it into smaller classes.}}
//^^^^^

  private $field;

  public function f() {
    return;
  }

}

new class { // Noncompliant {{This anonymous class has 6 lines, which is greater than the 5 authorized. Split it into smaller classes.}}
//  ^^^^^

  private $field;

  public function f() {
    return;
  }

};

