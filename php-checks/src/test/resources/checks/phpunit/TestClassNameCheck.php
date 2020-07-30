<?php

class TestClassA extends PHPUnit\Framework\TestCase {} // Noncompliant {{Rename this class to end with "Test" to ensure it will be executed by the PHPUnit CLI.}}
//    ^^^^^^^^^^
class Test extends PHPUnit\Framework\TestCase {} // OK
class FooTest extends PHPUnit\Framework\TestCase {} // OK
class FooTestCase extends PHPUnit\Framework\TestCase {} // Noncompliant
class TestClassB extends TestCase {} // OK
abstract class FooTestCase extends PHPUnit\Framework\TestCase {} // OK

$foo = new class extends PHPUnit\Framework\TestCase {}; // OK
