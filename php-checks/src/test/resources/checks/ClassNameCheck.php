<?php

class myClass {  // NOK {{Rename class "myClass" to match the regular expression ^[A-Z][a-zA-Z0-9]*$.}}
//    ^^^^^^^
}

class MyClass {  // OK
}

interface MyInterface { // OK
}

trait MyTrait {} // OK

class m150513_053633_add_dummy_user { // NOK {{Rename class "m150513_053633_add_dummy_user" to match the regular expression ^[A-Z][a-zA-Z0-9]*$.}}
}

interface My_Interface { // NOK {{Rename interface "My_Interface" to match the regular expression ^[A-Z][a-zA-Z0-9]*$.}}
}

trait My_Trait { // NOK {{Rename trait "My_Trait" to match the regular expression ^[A-Z][a-zA-Z0-9]*$.}}
}
