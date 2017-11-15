<?php


// ------------ test_static_field ----------------
class A {
  static $staticField = 2;

  function internalStatic() {
    echo $this::$staticField;
    echo self::$staticField;
    echo static::$staticField;
    $funcExpr = function(){ echo $this::$staticField; };
    $funcExpr();
  }

  function externalStatic($obj) {
    echo $obj::$staticField;                  // not supported as usage of static field $staticField
  }
}

(new A())->internalStatic();
(new A())->externalStatic(new A());




// ------------ test_field ----------------

class C {
  public $anotherClassField = 6;
}

class B {
  public $field = 1;
  public $fieldArray = [10, 15];

  function internalField() {
    echo $this->field;
    echo $this->fieldArray[1];
  }


  function externalField($obj1, $obj2) {
    echo $obj1->field;
    echo $obj2->anotherClassField;
  }

}

(new B())->internalField();
(new B())->externalField(new B(), new C());





// ------------ test_method ----------------

class F {

  function anotherClassMethod() {
    echo "anotherClassMethod\n";
  }
}

class E {
  public $method = "field\n";

  function method() {
    echo "method\n";
    return $this;
  }

  function copy() {
    return $this;
  }

  function testMethod($obj1, $obj2) {
    $this->method();
    $this->copy()->method();
    $this->method()->copy();
    echo $this->method;

    $funcExpr1 = function() { self::method(); };
    $funcExpr1();

    $obj1->method();
    $obj2->method();
    $obj2::anotherClassMethod();
  }

}
(new E())->testMethod(new E(), new F());




// ------------ test_static_method ----------------

class D {
  static $staticMethod = "staticField\n";

  static function staticMethod() {
    echo "static\n";
  }

  function testStaticMethod() {
    D::staticMethod();
    self::staticMethod();
    static::staticMethod();
    $this::staticMethod();
    $this->staticMethod();

    echo self::$staticMethod;
  }
}

(new D())->testStaticMethod();






// ------------ test_const_field ----------------

class M {
  public $constField = "just field\n";
  const constField = "const\n";

  function testConst() {
    echo $this::constField;  // const
    echo $this->constField;  // not const
  }
}

(new M())->testConst();






// ------------ test_used_before_declaration ----------------

class K {

   function earlyUseMethod() {
     echo $this->lateDeclField;
     $this->lateDeclMethod();
   }

   public $lateDeclField = "Field\n";

   function lateDeclMethod() {
     echo "Method\n";
   }
}

(new K())->earlyUseMethod();





// ------------- test_property_name_in_variable --------------

function test_property_name_in_variable($p){
  $a = "property";

  return $p->$a;
}




// --------------- test_real_code --------------

class R {
  public function __clone() {
      foreach($this as $key => $val) {
          if (is_object($val) || (is_array($val))) {
              $this->{$key} = unserialize(serialize($val));
          }
      }
  }
}


class RR {
   public $prop = 1;
}

$k = "prop";

// same effect
echo (new A)->prop;
echo (new A)->$k;


// --------------- test_local_var_as_members -----------

class RRR {
 public static function someStaticMethod() { echo "static!\n";}

 public $fieldToTestVariables = "field!\n";

 function A($funcN) {
   echo "constructor\n";
   $fieldN = "fieldToTestVariables";
   self::$funcN();
   echo $this->$fieldN;
 }

}


new A("someStaticMethod");


// --- test lookup parent class ---
class Parent {
   const A_CONST = 'a';
}

class Children extends Parent {
  public $arr = [
         self::A_CONST,
      ];
}
// class that verifies that no stackoverflow is thrown when name is considered the same symbol in class and extension
class BadMethodCallException extends \BadMethodCallException implements ExceptionInterface { }
