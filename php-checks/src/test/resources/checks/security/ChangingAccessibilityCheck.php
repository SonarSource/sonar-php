<?php

class MyClass
{
    public static $publicstatic = 'Static';
    private static $privatestatic = 'private Static';
    private $private = 'Private';
    private const CONST_PRIVATE = 'Private CONST';
    public $myfield = 42;

    private function __construct() {}
    private function privateMethod() {}
    public function __set($property, $value)  {}
    public function __get($property) {}
}

(new ReflectionClass('MyClass'))->getStaticProperties(); // Noncompliant
(new reflectionclass('MyClass'))->getstaticproperties(); // Noncompliant

$clazz = new ReflectionClass('MyClass');

$clazz->getStaticProperties(); // Noncompliant {{Make sure that this accessibility bypass is safe here.}}
$clazz->getSTATICPROPERTIES(); // Noncompliant

$clazz->setStaticPropertyValue('publicstatic', '42'); // OK as there is no overloading to bypass and it respects access control.
$clazz->getStaticPropertyValue('publicstatic'); // OK as there is no overloading to bypass and it respects access control.

// The following calls can access private or protected constants.
$clazz->getConstant('CONST_PRIVATE'); // Noncompliant
$clazz->getConstants(); // Noncompliant
$clazz->getReflectionConstant('CONST_PRIVATE'); // Noncompliant
$clazz->getReflectionConstants(); // Noncompliant

$obj = $clazz->newInstanceWithoutConstructor(); // Noncompliant

$constructorClosure = (new ReflectionClass('MyClass'))->getConstructor()->getClosure($obj); // Noncompliant
$constructorClosure = $clazz->getConstructor()->getClosure($obj); // Noncompliant
$constructor = $clazz->getConstructor();
$constructorClosure = $constructor->getClosure($obj); // Noncompliant
$constructor->setAccessible(true); // Noncompliant {{Make sure that this accessibility update is safe here.}}

$prop = new \ReflectionProperty('MyClass', 'private');
$prop->setAccessible(true); // Noncompliant {{Make sure that this accessibility update is safe here.}}
$prop->setValue($obj, "newValue"); // Noncompliant {{Make sure that this accessibility bypass is safe here.}}
$prop->getValue($obj); // Noncompliant

$prop2 = $clazz->getProperties()[2];
$prop2->setAccessible(true); // Noncompliant {{Make sure that this accessibility update is safe here.}}
$prop2->setValue($obj, "newValue"); // Noncompliant
$prop2->getValue($obj); // Noncompliant

$meth = new ReflectionMethod('MyClass', 'privateMethod');
$clos = $meth->getClosure($obj); // Noncompliant
$meth->setAccessible(true); // Noncompliant {{Make sure that this accessibility update is safe here.}}

$meth2 = $clazz->getMethods()[0];
$clos2 = $meth2->getClosure($obj); // Noncompliant
$meth2->setAccessible(true); // Noncompliant {{Make sure that this accessibility update is safe here.}}

// Using a ReflectionObject instead of the class

$objr = new ReflectionObject($obj);
$objr->newInstanceWithoutConstructor(); // Noncompliant

$objr->getStaticPropertyValue("publicstatic"); // OK as there is no overloading to bypass and it respects access control.
$objr->setStaticPropertyValue("publicstatic", "newValue"); // OK as there is no overloading to bypass and it respects access control.

$objr->getStaticProperties(); // Noncompliant

// The following calls can access private or protected constants.
$objr->getConstant('CONST_PRIVATE'); // Noncompliant
$objr->getConstants(); // Noncompliant
$objr->getReflectionConstant('CONST_PRIVATE'); // Noncompliant
$objr->getReflectionConstants(); // Noncompliant

$constructor = $objr->getConstructor();
$constructorClosure = $constructor->getClosure($obj); // Noncompliant
$constructor->setAccessible(true); // Noncompliant {{Make sure that this accessibility update is safe here.}}

$prop3 = $objr->getProperty('private');
$prop3->setAccessible(true); // Noncompliant
$prop3->setValue($obj, "newValue"); // Noncompliant
$prop3->getValue($obj); // Noncompliant

$prop4 = $objr->getProperties()[2];
$prop4->setAccessible(true); // Noncompliant {{Make sure that this accessibility update is safe here.}}
$prop4->setValue($obj, "newValue"); // Noncompliant
$prop4->getValue($obj); // Noncompliant

$meth3 = $objr->getMethod('privateMethod');
$clos3 = $meth3->getClosure($obj); // Noncompliant
$meth3->setAccessible(true); // Noncompliant {{Make sure that this accessibility update is safe here.}}

$meths = $objr->getMethods();
$meth4 = $meths[0];
$clos4 = $meth4->getClosure($obj); // Noncompliant
$meth4->setAccessible(true); // Noncompliant {{Make sure that this accessibility update is safe here.}}

foreach ($meths as $meth5){
  $meth5->getClosure($obj); // Noncompliant
}

foreach ($objr->getMethods() as $meth6){
  $meth6->getClosure($obj); // Noncompliant
}

foreach ($objr->getMethods() as $key => $meth7){
  $key->getClosure($obj);
  $meth7->getClosure($obj); // Noncompliant
}
