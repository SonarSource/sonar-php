<?php

class A {

  const CONSTANT = 0;


  public static $field;

  public $arr = array(
    self::CONSTANT             // OK
  );

  const ARRAY_CONST = array(
      self::CONSTANT           // OK
  );

  public static function f1() {
    return self::$field;       // NOK
  }

  public static function f2() {
    return static::$field;     // OK
  }

}
