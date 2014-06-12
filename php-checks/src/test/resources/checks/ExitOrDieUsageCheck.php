<?php

class Foo
{

    public function bar($param)
    {

        if ($param) {
            exit(23); // NOK
        } else {
            die(24);  // NOK
        }
    }
}
