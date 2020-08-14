<?php

class Class1 {}
class Class2 extends Class1 {}
class Class3 extends Class2 {}
class Class4 extends Class3 {}
class Class5 extends Class4 {}
class Class6 extends Class5 {} // OK
class Class7 extends Class6 {} // Noncompliant {{This class has 6 parents which is greater than 5 authorized.}}
//    ^^^^^^

class ClassB extends BuiltInClass {} // FN - if BuiltInClass has 5 parents

new class extends Class6 {}; // Noncompliant
//  ^^^^^

new class extends BuiltInClass {}; // FN - if BuiltInClass has 5 parents

new class {};
