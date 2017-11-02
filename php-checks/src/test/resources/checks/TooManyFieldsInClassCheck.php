<?php

  class A {        // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
//^^^^^
  public $f1;
  public $f2;
  public $f3;
  protected $f4;
  private $f5;
}


class B {
  public $f1;
  public $f2;
}

$x = new class {        // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
//       ^^^^^
  public $f1;
  public $f2;
  public $f3;
  protected $f4;
  private $f5;
};


$x = new class {
  public $f1;
  public $f2;
};
