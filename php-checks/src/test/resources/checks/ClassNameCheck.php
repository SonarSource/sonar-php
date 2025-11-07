<?php

use yii\db\Migration;

class myClass {  // NOK {{Rename class "myClass" to match the regular expression ^[A-Z][a-zA-Z0-9]*$.}}
//    ^^^^^^^
}

class MyClass {  // OK
}

interface myInterface { // OK
}

class yii_db_migration_238172398123 extends yii\db\Migration { } // OK Exception for specific Yii generated db migration classes
class yii_db_migration_726386234872634 extends Migration { } // OK
