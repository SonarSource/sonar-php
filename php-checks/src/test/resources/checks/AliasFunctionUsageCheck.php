<?php

class Foo
{

    public function bar($param1, $param2)
    {
        sizeof($param1); // NOK
        count($param1); // OK

        delete($param1); // NOK
        unset($param1); // OK

        print($param); // NOK
        echo($param1); // OK

        is_null($param1); // NOK
        $param1 === null; // OK

        is_double($param1); // NOK
        is_float($param1); // OK

        is_integer($param1); // NOK
        is_int($param1); // OK

        is_long($param1); // NOK
        is_int($param1); // OK

        is_real($param1); // NOK
        is_float($param1); // OK

        create_function($param1); // NOK

        chop($param1); // NOK
        rtrim($param1); // OK

        ini_alter($param1); // NOK
        ini_set($param1); // OK

        join($param1); // NOK
        implode($param1); // OK

        key_exists($param1, $param2); // NOK
        array_key_exists($param1, $param2); // OK

        fputs($param1); // NOK
        fwrite($param1); // OK

        is_writeable($param1); // NOK
        is_writable($param1); // OK
    }
}