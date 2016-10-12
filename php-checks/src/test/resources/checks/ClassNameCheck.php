<?php

class myClass {  // NOK {{Rename class "myClass" to match the regular expression ^[A-Z][a-zA-Z0-9]*$.}}
//    ^^^^^^^
}

class MyClass {  // OK
}

interface myInterface { // OK
}
