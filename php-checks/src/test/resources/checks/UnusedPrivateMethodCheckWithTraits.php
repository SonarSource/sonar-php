<?php

class MagicMethodCall
{
  use MyTraitWithMagicMethodCallWithCallUserFuncArrayFunction;

  private function bar()
  {
  // Compliant as bar() can be called in the trait
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

class MagicMethodCallFalseNegative
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

class MagicMethodCall2
{
  use MyTraitWithoutMagicMethodCall;

  private function bar() // Noncompliant
  {
  }
}

trait MyTraitWithoutMagicMethodCall
{
  public function foo()
  {
  }
}

class MagicMethodCall2
{
  use NonExistingTrait;

  private function bar() // Noncompliant
  {
  }
}
