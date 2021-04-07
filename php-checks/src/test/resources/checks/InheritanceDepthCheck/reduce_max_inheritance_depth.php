<?php

class Class1 {}
class Class2 extends Class1 {}
class Class3 extends Class2 {}
class Class4 extends Class3 {}
class Class5 extends Class4 {} // Noncompliant {{This class has 4 parents which is greater than 3 authorized.}}
//    ^^^^^^
