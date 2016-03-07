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
    self::f2();                // NOK
    self::f3();                // OK, "f3" can't be overridden
    return self::$field;       // NOK {{Use "static" keyword instead of "self".}}
  }

  public static function f2() {
    return static::$field;     // OK
  }

  public static final function f3() {
  }

}

final class B {
  public static $field;

  public static function f1() {
    self::f2();                // OK, class is final
    return self::$field;       // OK, class is final
  }

  public static function f2() {}
}

class D {
  public static function f1() {
    self::f2();      // NOK
    class C {
      static $field;
      public static function f1() {
        self::f2();                // OK
        return self::$field;       // NOK
      }

      public static final function f2() {}
    }
  }

  public static function f2() {}
}
