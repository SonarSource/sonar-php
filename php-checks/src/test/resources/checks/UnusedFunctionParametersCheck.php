<?php

function f($p1, $p2, $p3) {              // Noncompliant {{Remove the unused function parameter "$p1".}}
//         ^^^
    $p2 = 1;
    call($p3);
}

$a = function($p1, $p2) { return $p1; }; // Noncompliant {{Remove the unused function parameter "$p2".}}

$a = function($p1, $p2) {
    func_get_args();
    return $p1;
};

function f($p1, $p2) {                   // Noncompliant {{Remove the unused function parameter "$p1".}}
  function nestedF($p1, $p2) {           // Noncompliant {{Remove the unused function parameter "$p2".}}
    $p1 = 1;
  }
  return $p2;
}

function f($p1){
    call($p2);
    func_get_args();
    call($p2);
    $a = function($p2) {
        func_get_args();
        $b = function($p3){				// Noncompliant {{Remove the unused function parameter "$p3".}}
        $c = function($p4){ func_get_args();};
        };
    };
    call($p2);
}

function containing_func_get_args_with_one_not_explicitly_used_parameter($p1) {
    func_get_args();
    call($p2);
}

function containing_func_get_args_with_only_not_explicitly_used_parameters($p1, $p2) {
    $args = func_get_args();
}

function containing_func_get_args_with_some_not_explicitly_used_parameters($p1, $p2, $p3) {
    call($p2);
    print_r(func_get_args());
}

// Noncompliant@+1 {{Remove the unused function parameter "$p1".}}
function containing_commented_func_get_args_with_some_not_explicitly_used_parameters($p1, $p2, $p3) {     // Noncompliant {{Remove the unused function parameter "$p3".}}
    call($p2);
    //func_get_args();
}

function containing_func_get_args_with_explicitly_used_parameters($p1, $p2) {
    func_get_args();
    $p1 = $p2;
    return $p3;
}

function containing_func_get_args_with_no_parameters() {
    func_get_args();
    $p1 = 42;
    return $p1;
}

function f($p1, $p2) {                   // Noncompliant {{Remove the unused function parameter "$p1".}}
    function containing_func_get_args_with_some_not_explicitly_used_parameters_in_a_nestedFunction($p1, $p2) {
        $p1 = 1;
        func_get_args();
    }
    return $p2;
}

class C {

// Noncompliant@+1 {{Remove the unused function parameter "$p2".}}
  public function f1($p1, $p2, $p3) {    // Noncompliant {{Remove the unused function parameter "$p3".}}
      return $p1;
  }

  public function f2($p1) {              // Noncompliant
    return 1;
  }

  public function f3($p1);               // OK

  public function containing_func_get_args_with_some_not_explicitly_used_parameters_inside_a_class($p1, $p2, $p3,$p4) {
      $p1 = $p2;
      func_get_args();
      return $p5;
  }
}

function f($p1, $p2) {                   // OK
  $p1 = $p2;
}

class A {
  public function f1($p1) {$a = $p1;}
}

class D extends A {
  public function f1($p1) {} // OK
  private function f2($p1) {}               // Noncompliant {{Remove the unused function parameter "$p1".}}
  public function containing_func_get_args_with_only_not_explicitly_used_parameters_inside_a_subclass($p1, $p2, $p3,$p4) {
      func_get_args();
      return $p5;
  }
}

class E implements B {

  public function f1($p1) {} // OK

  public function f2() {
    $f = function($p1) {};    // Noncompliant
  }
  public function f3() {
    $f = function($p1) {
        func_get_args();
    };
  }

  public function containing_func_get_args_with_no_parameters_inside_a_class_implementing_an_interface($p1, $p2, $p3,$p4) {
      func_get_args();
      return $p5;
  }
}

class K {
  public function f1($p1, $p2, $p3, $var) {    // OK
      doSomething("${var}");
      return "$p1 ${p2} {$p3}";
  }
}

function foo($p1) {   // OK
   f1 = function() use ($p1) {
     echo $p1;
   };
}

function foo($variable) {  // OK
    $array = compact('variable');
    return $array;
}

function bar($p) {
    $vars = 'p';
    return compact($vars);
}

function bar2($p) { // Noncompliant
    $vars = 'p';
    return compact('a');
}


class Foo {
  public function f1($p1) {                   // Noncompliant {{Remove the unused function parameter "$p1".}}
    $x = new class extends A {
     public function f1($p1) {}                 // OK
     private function f2($p1) {}               // Noncompliant {{Remove the unused function parameter "$p1".}}
    };
 }

  public function f2($p1) {                   // Noncompliant {{Remove the unused function parameter "$p1".}}
    $x = new class extends A {
     public function f1($p1) {}                 // OK
     private function containing_func_get_args_with_some_not_explicitly_used_parameters_inside_anonymous_subclass_($p1, $p2, $p3) {
         call($p1);
         func_get_args();
     }
    };
  }
}

function executionOperator($p) {
  $result = `ls $p`;
}

//------------ INTERFACES--------------

interface Interface1 extends Interface2, Interface3, UnknownInterface {
  public function interfaceMethod1($a); // OK
  public function interfaceMethod2($a); // OK
}

interface Interface2 {
  public function interfaceMethod4($a);
}

interface Interface3 {
  public function interfaceMethod4($a);
}

abstract class ImplementingClass1 implements Interface1 {
  public function interfaceMethod1($a) {} // OK
  private function foo($b) {return $b;}
  public function interfaceMethod4($a) {}
}

class ImplementingClass2 extends ImplementingClass1 implements UnknownInterface {
  public function interfaceMethod2($a) {} // OK
  private function foo($b) {} // Noncompliant
  public function interfaceMethod3($a) {} // OK - can be defined in UnknownInterface
}

class ImplementingClass3 implements Interface3 {
  public function interfaceMethod5($a) {} // Noncompliant
}

abstract class AbstractController {
  public function publicMethod($request) {} // OK
  private function privateMethod($request) {} // Noncompliant
  protected function protectedMethod($request) {} // OK
}

class Php8Class
{
  public function __construct(private $a) {} // Compliant

  public function __construct($a) {} // Noncompliant

  public function __construct(private $a, $b) {} // Noncompliant
//                                        ^^
}

// Compliant: exclude underscore as a convention for unused variable
$x = function($p1, $_, $__) {
  return $p1;
};

// Underscore as a prefix has no special meaning
$x = function($p1, $_p2, $__p3) { // Noncompliant 2
  return $p1;
};

// Magento plugin methods - before*, around*, after*
namespace Vendor\Module\Plugin;

class ExamplePlugin
{
    // Before method with 3 parameters - should not raise issues
    public function beforeSomeMethod($subject, $arg1, $arg2)
    {
        return;
    }

    // Around method with 4 parameters - should not raise issues
    public function aroundSomeMethod($subject, callable $proceed, $arg1, $arg2)
    {
        return;
    }

    // After method with 2 parameters - should not raise issues
    public function afterSomeMethod($subject, $result)
    {
        return;
    }

    // Before method with more than 3 parameters - should not raise issues
    public function beforeAnotherMethod($subject, $arg1, $arg2, $arg3, $arg4)
    {
        return;
    }

    // Around method with more than 4 parameters - should not raise issues
    public function aroundAnotherMethod($subject, callable $proceed, $arg1, $arg2, $arg3)
    {
        return;
    }

    // After method with more than 2 parameters - should not raise issues
    public function afterAnotherMethod($subject, $result, $extra)
    {
        return;
    }

    // Before method with less than 3 parameters - should raise issues
    public function beforeInvalid($subject, $arg1) // Noncompliant 2
    {
        return;
    }

    // Around method with less than 4 parameters - should raise issues
    public function aroundInvalid($subject, callable $proceed, $arg1) // Noncompliant 3
    {
        return;
    }

    // After method with less than 2 parameters - should raise issues
    public function afterInvalid($subject) // Noncompliant
    {
        return;
    }

    // Method not starting with before/around/after - should raise issues normally
    public function normalMethod($subject, $arg1, $arg2) // Noncompliant 3
    {
        return;
    }
}

// Magic methods - methods starting with __ should not raise issues
class MagicMethodsClass
{
    // Destructor - should not raise issues
    public function __destruct()
    {
        return;
    }

    // ToString - should not raise issues
    public function __toString($param1)
    {
        return;
    }

    // Setter - should not raise issues
    public function __set($param1,$param2)
    {
        return;
    }

    public function __construct(private $promoted, $used, $unused) { //Noncompliant
         print($used);
    }

    // Regular method starting with __ (but not a standard magic method) - should raise issues
    public function __customMagicMethod($param1, $param2) // Noncompliant 2
    {
        return;
    }

    // Non-magic method - should raise issues normally
    public function regularMethod($param1, $param2) // Noncompliant 2
    {
        return;
    }
}


