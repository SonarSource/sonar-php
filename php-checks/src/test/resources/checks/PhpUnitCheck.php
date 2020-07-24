<?php

use PHPUnit\Framework\TestCase;

class MyTest extends TestCase {
  public function myTestMethod() {} // Noncompliant {{Identified as test method.}}
}

class MyOtherTest extends PHPUnit\Framework\TestCase {
  public function myTestMethod() {} // Noncompliant {{Identified as test method.}}
}

class MyOtherTest extends PHPUnit_Framework_TestCase {
  public function myTestMethod() {} // Noncompliant {{Identified as test method.}}
}

class MyNormalTest extends FooTest {
  public function myTestMethod() {} // OK
}
