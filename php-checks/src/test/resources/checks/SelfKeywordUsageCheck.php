<?php

class A {

  const CONSTANT = 0;

  private static $field_private;

  protected static $field_protected;

  public static $field_public;

  static $field_default;

  public $arr = array(
    self::CONSTANT             // OK
  );

  const ARRAY_CONST = array(
    self::CONSTANT             // OK
  );

  public static function f1() {
    self::f2();                // NOK
//  ^^^^
    self::f3();                // OK, "f3" can't be overridden
    return self::$field_public; // NOK {{Use "static" keyword instead of "self".}}
  }

  private static function f2_private() {}

  protected static function f2_protected() {}

  public static function f2_public() {}

  static function f2_default() {}
 
  public static final function f3() {
  }

  private static final function f4() {
  }

  public static function f4() {
    self::$field_private;       // OK, as field is private
    self::$field_protected;     // NOK {{Use "static" keyword instead of "self".}}
    self::$field_public;        // NOK
    self::$field_default;       // NOK

    static::$field_private;     // OK, as always with "static"
    static::$field_protected;   // OK, as always with "static"
    static::$field_public;      // OK, as always with "static"
    static::$field_default;     // OK, as always with "static"

    self::f2_private();         // OK, as function is private
    self::f2_protected();       // NOK {{Use "static" keyword instead of "self".}}
    self::f2_public();          // NOK
    self::f2_default();         // NOK
    
    self::f4();                 // OK, as function is private (and final as well)

    static::f2_private();       // OK, as always with "static"
    static::f2_protected();     // OK, as always with "static"
    static::f2_public();        // OK, as always with "static"
    static::f2_default();       // OK, as always with "static"

    self::f3();                 // OK, "f3" can't be overridden
  }

}

final class B {

  private static $field_private;

  public static $field_public;

  public static function f1() {
    self::f2();                // OK, class is final

    self::$field_private;      // OK, class is final and field is private
    return self::$field_public;// OK, class is final
  }

  public static function f2() {}
}

class D {
  public static function f1() {
    self::f2();                      // NOK
    self::f3();                      // OK, function is private
    class C {
      private static $field_private;
      public static $field_public;

      public static function f1() {
        static::f2();                // OK, as always with "static"
        self::f2();                  // OK, function is final
        self::f4();                  // OK, function is private
        self::f3();                  // NOK, function is public
        self::$field_private;        // OK, field is private
        return self::$field_public;  // NOK, field is public
      }

      public static final function f2() {}
      private static  function f4() {}
      public static function f3() {}
    }
    self::f2();                      // NOK
    self::f3();                      // OK, function is private
  }
  
  private static function f3() {}

  public static function f2() {}
}
