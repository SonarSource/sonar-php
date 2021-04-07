<?php

class Class1 {}
//    ^^^^^^> {{Parent class.}}
class Class2 extends Class1 {}
//    ^^^^^^> {{Parent class.}}
class Class3 extends Class2 {}
//    ^^^^^^> {{Parent class.}}
class Class4 extends Class3 {}
//    ^^^^^^> {{Parent class.}}
class Class5 extends Class4 {}
//    ^^^^^^> {{Parent class.}}
class Class6 extends Class5 {} // OK
//    ^^^^^^> {{Parent class.}}
class Class7 extends Class6 {} // Noncompliant {{This class has 6 parents which is greater than 5 authorized.}}
//    ^^^^^^

class ClassB extends BuiltInClass {} // FN - if BuiltInClass has 5 parents

new class extends Class6 {}; // Noncompliant
//  ^^^^^

new class extends BuiltInClass {}; // FN - if BuiltInClass has 5 parents

new class {};

class ExtendingItself extends ExtendingItself {}

class SuperclassCycleClass1 extends SuperclassCycleClass2 {}
class SuperclassCycleClass2 extends SuperclassCycleClass3 {}
class SuperclassCycleClass3 extends SuperclassCycleClass1 {}
