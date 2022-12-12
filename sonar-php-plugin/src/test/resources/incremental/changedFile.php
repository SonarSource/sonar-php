<?php

namespace App\Test;

use FooClass;

class Controller
{
  private FooClass $foo;
  public function __construct(FooClass $foo)
  {
    $this->foo = $foo;
  }

  public function doSomething()
  {
    $this->foo->fooFunction();
  }

  public function doSomethingElse()
  {
    $this->foo->foo();
  }
}
