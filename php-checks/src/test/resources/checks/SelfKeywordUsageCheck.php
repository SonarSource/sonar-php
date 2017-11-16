<?php

class A {

  const CONSTANT = 0;
  const CONSTANT1 = 1, CONSTANT2 = 2;

  private static $field_private;

  protected static $field_protected;

  public static $field_public;

  static $field_default;

  public $arr = array(
    self::CONSTANT             // OK, inside initializer (only constants can be used)
  );

  const ARRAY_CONST = array(
    self::CONSTANT             // OK, inside initializer
  );

  public static function f1() {
    self::f2();                // Noncompliant
//  ^^^^
    self::CONSTANT;            // OK, constant
    self::CONSTANT1;            // OK, constant
    self::CONSTANT2;            // OK, constant
    self::f3();                // OK, "f3" can't be overridden
    return self::$field_public; // Noncompliant {{Use "static" keyword instead of "self".}}
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
    self::$field_protected;     // Noncompliant {{Use "static" keyword instead of "self".}}
    self::$field_public;        // Noncompliant
    self::$field_default;       // Noncompliant

    static::$field_private;     // OK, as always with "static"
    static::$field_protected;   // OK, as always with "static"
    static::$field_public;      // OK, as always with "static"
    static::$field_default;     // OK, as always with "static"

    self::f2_private();         // OK, as function is private
    self::f2_protected();       // Noncompliant {{Use "static" keyword instead of "self".}}
    self::f2_public();          // Noncompliant
    self::f2_default();         // Noncompliant
    
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
    self::f2();                      // Noncompliant
    self::f3();                      // OK, function is private
    class C {
      private static $field_private;
      public static $field_public;

      public static function f1() {
        static::f2();                // OK, as always with "static"
        self::f2();                  // OK, function is final
        self::f4();                  // OK, function is private
        self::f3();                  /* Noncompliant */ // function is public
        self::$field_private;        // OK, field is private
        return self::$field_public;  /* Noncompliant */ // field is public
      }

      public static final function f2() {}
      private static  function f4() {}
      public static function f3() {}
    }
    self::f2();                      // Noncompliant
    self::f3();                      // OK, function is private
  }
  
  private static function f3() {}

  public static function f2() {}
}

class E {
    const A_CONST = 'a';
}
class F extends E {
    const B_CONST = 'b';
    public $arr = [
       self::B_CONST,
       self::A_CONST,
    ];
}

class RegionBundleTest extends \PHPUnit_Framework_TestCase
{
    const RES_DIR = '/base/region';

    /**
     * @var RegionBundle
     */
    private $bundle;

    /**
     * @var \PHPUnit_Framework_MockObject_MockObject
     */
    private $reader;

    protected function setUp()
    {
        $this->reader = $this->getMock('Symfony\Component\Intl\ResourceBundle\Reader\StructuredBundleReaderInterface');
        $this->bundle = new RegionBundle(self::RES_DIR, $this->reader);
    }

    public function testGetCountryName()
    {
        $this->reader->expects($this->once())
            ->method('readEntry')
            ->with(self::RES_DIR, 'en', array('Countries', 'AT'), true)
            ->will($this->returnValue('Austria'));

        $this->assertSame('Austria', $this->bundle->getCountryName('AT', 'en'));
    }

    public function testGetCountryNames()
    {
        $sortedCountries = array(
            'AT' => 'Austria',
            'DE' => 'Germany',
        );

        $this->reader->expects($this->once())
            ->method('readEntry')
            ->with(self::RES_DIR, 'en', array('Countries'), true)
            ->will($this->returnValue($sortedCountries));

        $this->assertSame($sortedCountries, $this->bundle->getCountryNames('en'));
    }
}

class constantInParams {
 const DATASERIES_TYPE_STRING	= 'String';
 public function setDataType($dataType = self::DATASERIES_TYPE_STRING) {
 }
}
