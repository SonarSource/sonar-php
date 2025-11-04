<?php

use yii\db\Migration;


class myClass {  // NOK {{Rename class "myClass" to match the regular expression ^[a-z0-9_]+$}}
//    ^^^^^^^
}

class m150513_053633_add_dummy_user { //OK
}
