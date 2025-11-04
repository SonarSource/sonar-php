<?php

class myClass {  // NOK {{Rename class "myClass" to match the regular expression ^[A-Z][a-zA-Z0-9]*$.}}
//    ^^^^^^^
}

class MyClass {  // OK
}

interface MyInterface { // OK
}

class m150513_053633_add_dummy_user { // NOK {{Rename class "m150513_053633_add_dummy_user" to match the regular expression ^[A-Z][a-zA-Z0-9]*$.}}
}
