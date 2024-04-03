<?php

function f($p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8) { // Noncompliant {{This function has 8 parameters, which is greater than the 2 authorized.}}
//        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

function g($p1, $p2, $p3) { // Noncompliant
}

function h($p1, $p2) {
}

class A {
  function __construct($p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8) { // Noncompliant
  }  
}

class B {
  function __construct($p1, $p2, $p3, $p4, $p5, $p6, $p7) {
  }
}

class C {
  function f($p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8) { // Noncompliant
  }
}

$x = new class() {
  function __construct($p1, $p2, $p3, $p4, $p5, $p6, $p7) {
  }
};

class C1 extends C {
  function f($p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8) {
  }
  function g($p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8) { // Noncompliant
  }
}

class OnePromotedProperty {
  function __construct(
    public $p1,
    $p2) {
  }
}

class OnlyPromotedProperties {
  function __construct(
    public $p1,
    PUBLIC $p2,
    protected $p3,
    PROTECTED $p4,
    private $p5,
    PRIVATE $p6,
    readonly PRIVATE $p7,
    PRIVATE READONLY $p8) {
  }
}

class ReadOnlyPromotedProperty {
  function __construct(  // Noncompliant {{This function has 10 parameters, which is greater than the 7 authorized.}}
    public readonly $p1,
    readonly public $p2,
    $p3,
    $p4,
    $p5,
    $p6,
    $p7,
    $p8,
    $p9,
    $p10,
    $p11,
    $p12) {
  }
}

class OnePromotedPropertyAndEightParameters {
  function __construct( // Noncompliant {{This function has 8 parameters, which is greater than the 7 authorized.}}
    $p1,
    $p2,
    $p3,
    $p4,
    $p5,
    $p6,
    $p7,
    $p8,
    public $p9) {
  }
}

class OneProtectedPromotedPropertyAndEightParameters {
  function __construct(  // Noncompliant {{This function has 8 parameters, which is greater than the 7 authorized.}}
    $p1,
    $p2,
    $p3,
    $p4,
    $p5,
    $p6,
    $p7,
    $p8,
    protected $p9) {
  }
}

class OnePrivatePromotedPropertyAndEightParameters {
  function __construct(  // Noncompliant {{This function has 8 parameters, which is greater than the 7 authorized.}}
    $p1,
    $p2,
    $p3,
    $p4,
    $p5,
    $p6,
    $p7,
    $p8,
    private $p9) {
  }
}

class OnePrivateReadOnlyPromotedPropertyAndEightParameters {
  function __construct(  // Noncompliant {{This function has 8 parameters, which is greater than the 7 authorized.}}
    $p1,
    $p2,
    $p3,
    $p4,
    $p5,
    $p6,
    $p7,
    $p8,
    private readonly $p9) {
  }
}
