<?php

class A {

  public static $field;

  public static function f() {
    return self::$field;       // NOK
  }

  public static function f() {
    return static::$field;     // OK
  }

}
