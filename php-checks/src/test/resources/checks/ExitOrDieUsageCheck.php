<?php

class Foo
{

    public function bar($param)
    {

        if ($param) {
            exit(23); // NOK {{Remove this "exit()" call or ensure it is really required}}
        } else {
            die(24);  // NOK {{Remove this "die()" call or ensure it is really required}}
        }
    }
}
