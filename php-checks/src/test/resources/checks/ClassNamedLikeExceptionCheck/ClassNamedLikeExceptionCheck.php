<?php

// no extend
class AMyException {} // Noncompliant {{Rename this class to remove "Exception" or correct its inheritance.}}

// single extend
class BMyException extends Foo {} // Compliant
class CMyException extends Exception {} // Compliant
class MyException extends \Exception {} // Compliant

// double extend
class DMyException extends Parent1 {} // Noncompliant
//    ^^^^^^^^^^^^
class Parent1 {}

class EMyException extends Parent2 {} // Compliant
class Parent2 extends Exception {}

// triple extend
class FMyException extends Parent3 {} // Noncompliant
class Parent3 extends Parent4 {}
class Parent4 {}

class JMyException extends Parent5 {} // Compliant
class Parent5 extends Parent6 {}
class Parent6 extends Exception {}

// the analysis is recursive. This shouldn't lead to infinite recursion.
class XException extends B {} // Noncompliant
class B extends C {}
class C extends XException {}

class X extends X {}

interface ZException {} // Compliant
interface ZException extends FooInterface {} // Compliant
class ZException implements FooInterface {} // Noncompliant

class my_exception {} // Noncompliant
class MY_EXCEPTION {} // Noncompliant
class MY_EXCEPTION_WRAPPER {} // Compliant
