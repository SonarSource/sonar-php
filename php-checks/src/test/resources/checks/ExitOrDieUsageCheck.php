<?php

class Foo
{

    public function bar($param)
    {

        if ($param) {
            exit(23); // Noncompliant {{Remove this "exit()" call or ensure it is really required}}
//          ^^^^
        } else {
            die(24);  // Noncompliant {{Remove this "die()" call or ensure it is really required}}
        }
    }
}
