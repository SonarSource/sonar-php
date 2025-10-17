<?php

class C {

  public function __construct($a, $b) {
    if ($a) {
      if ($b) {
        $object = new \SomeClass();                    // Noncompliant {{Remove this creation of object in constructor. Use dependency injection instead.}}
        $object = new SomeClass();                     // Noncompliant
//                ^^^
        $object = new Package\SomeOtherClass();        // Noncompliant
      } else {
        throw new InvalidArgumentException();
      }
    }
  }

  public function f() {
    $object = new SomeClass();                          // OK
  }
}

class D {

  public function D($a, $b) {
        $object = new SomeClass();                     // Noncompliant
  }

  public function f() {
    $object = new SomeClass();                          // OK
  }
}

class ClassWithoutConstructor {

  public function f() {
    $object = new SomeClass();                          // OK
  }
}

$object = new SomeClass();                             // OK

class A {
  function __construct() {
    $x = new class {                // Noncompliant
      function foo() {
        new Foo();                  // OK
      }

      function __construct() {
        new Foo();                  // Noncompliant
      }

      function bar() {
        new Foo();                  // OK
      }
    };
  }

  function foo() {
    $x = new class {                // OK
      function __construct() {
        new Foo();                  // Noncompliant
      }

      function bar() {
        new Foo();                  // OK
      }
    };
  }
}

class ConstructorWithDefaultParameters {
  // Default parameter values should NOT be flagged
  public function __construct(
    $object = new SomeClass()       // OK - default parameter value
  ) {
  }
}

class ConstructorWithMultipleDefaults {
  public function __construct(
    $a = new ClassA(),              // OK
    $b = new ClassB()               // OK
  ) {
  }
}

class ConstructorWithDefaultAndOther {
  public function __construct(
    $name,
    $object = new SomeClass()       // OK - default parameter value
  ) {
  }
}

class ConstructorMixedDefaultAndCreation {
  public function __construct(
    $object = new SomeClass()       // OK - default parameter value
  ) {
    $this->config = new Config();   // Noncompliant {{Remove this creation of object in constructor. Use dependency injection instead.}}
  }
}

class ConstructorWithComplexDefault {
  public function __construct(
    $object = new Namespace\SomeClass()  // OK - namespaced default
  ) {
  }
}

// Edge cases for parameter default detection
class ConstructorWithConditionalInBody {
  public function __construct(
    $dependency = new DefaultDependency()  // OK - default parameter
  ) {
    if ($dependency) {
      $this->other = new OtherClass();     // Noncompliant {{Remove this creation of object in constructor. Use dependency injection instead.}}
    }
  }
}

class ConstructorWithNestedDefaults {
  public function __construct(
    $a = new ClassA(),                     // OK
    $b = new ClassB(),                     // OK
    $c = new ClassC()                      // OK
  ) {
    $this->instance = new SomeInstance();  // Noncompliant {{Remove this creation of object in constructor. Use dependency injection instead.}}
  }
}

class ConstructorWithDefaultCallArgument {
  public function __construct(
    $factory = new Factory(),              // OK
    $config = array(
      'key' => 'value'
    )
  ) {
    $this->service = new Service();        // Noncompliant {{Remove this creation of object in constructor. Use dependency injection instead.}}
  }
}

class NoConstructor {
  public function someMethod() {
    return new SomeObject();               // OK - not in constructor
  }
}

class ConstructorWithTernaryOperator {
  public function __construct(
    $obj = true ? new TrueClass() : new FalseClass()  // OK - both are default values
  ) {
    $this->other = new OtherClass();        // Noncompliant {{Remove this creation of object in constructor. Use dependency injection instead.}}
  }
}
