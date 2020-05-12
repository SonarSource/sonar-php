<?php

// no extend
class AMyException {} // Noncompliant

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
