<?php

use A\B\FieldType;
use A\B\ParamType;
use A\B\ReturnType;

class MyClass {
  public FieldType $field;

  public function foo(ParamType $parameter): ReturnType {}
}
