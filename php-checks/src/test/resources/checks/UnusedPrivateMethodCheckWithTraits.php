<?php

class MagicMethodCallBase
{
  use MyTraitWithMagicMethodCallWithCallUserFuncArrayFunction;

  private function bar()
  {
  // Compliant as bar() can be called in the trait
  }
}

class MagicMethodCallChild extends MagicMethodCallBase
{

  // TODO SONARPHP-1482 FP because we can't access traits of a superclass in symbols
  private function foo() // Noncompliant
  {
  }
}

trait MyTraitWithMagicMethodCallWithCallUserFuncArrayFunction
{
  public function __call($method, $arguments)
  {
    if (method_exists($this, $method)) {
      return call_user_func_array([$this, $method], $arguments);
    }
    trigger_error('Call to undefined method '.__CLASS__.'::'.$method.'()', E_USER_ERROR);
  }
}

class MagicMethodCallFalseNegativeBase
{
  use MyTraitWithMagicMethodCallWithoutBody;

  // TODO SONARPHP-1481 FN because in trait in __call the call of call_user_func_array or call_user_func is missing
  private function bar()
  {
  }
}

trait MyTraitWithMagicMethodCallWithoutBody
{
  public function __call($method, $arguments)
  {
  // The call of call_user_func_array or call_user_func is missing
  }
}

class MagicMethodCallBase2
{
  use MyTraitWithoutMagicMethodCall;

  private function bar() // Noncompliant
  {
  }
}

class MagicMethodCallChild2 extends MagicMethodCallBase2
{
  private function foo() // Noncompliant
  {
  }
}

trait MyTraitWithoutMagicMethodCall
{
  public function foo()
  {
  }
}

class MagicMethodCall3
{
  use NonExistingTrait;

  private function bar() // Noncompliant
  {
  }
}

class MagicMethodCall4 extends MagicMethodCall3
{
  private function foo() // Noncompliant
  {
  }
}
