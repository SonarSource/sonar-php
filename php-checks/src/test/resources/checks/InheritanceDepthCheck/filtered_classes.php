<?php

class Class1 {}
class Class2 extends Class1 {}
class Class3 extends Class2 {}
class Class4 extends Class3 {}
class Class5 extends Class4 {}
class filteredClass1 extends Class5 {}
class Class7 extends filteredClass1 {} // OK

class filteredClass2 extends Class4 {}
class Class6 extends filteredClass2 {}
class Class7 extends Class6 {} //OK

