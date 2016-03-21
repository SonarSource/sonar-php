<?php

class Foo
{

    public function bar($param1, $param2)
    {
        sizeof($param1); // NOK
        SIZEOF($param1); // NOK
        count($param1); // OK

        delete($param1); // NOK
        DELETE($param1); // NOK
        unset($param1); // OK

        print($param); // NOK
        PRINT($param); // NOK
        echo($param1); // OK

        is_null($param1); // NOK
        IS_NULL($param1); // NOK
        $param1 === null; // OK

        is_double($param1); // NOK
        IS_DOUBLE($param1); // NOK
        is_float($param1); // OK

        is_integer($param1); // NOK
        IS_INTEGER($param1); // NOK
        is_int($param1); // OK

        is_long($param1); // NOK
        IS_LONG($param1); // NOK
        is_int($param1); // OK

        is_real($param1); // NOK
        IS_REAL($param1); // NOK
        is_float($param1); // OK

        create_function($param1); // NOK
        CREATE_FUNCTION($param1); // NOK

        chop($param1); // NOK
        CHOP($param1); // NOK
        rtrim($param1); // OK

        ini_alter($param1); // NOK
        INI_ALTER($param1); // NOK
        ini_set($param1); // OK

        join($param1); // NOK
        JOIN($param1); // NOK
        implode($param1); // OK

        key_exists($param1, $param2); // NOK
        KEY_EXISTS($param1, $param2); // NOK
        array_key_exists($param1, $param2); // OK

        fputs($param1); // NOK
        FPUTS($param1); // NOK
        fwrite($param1); // OK

        is_writeable($param1); // NOK
        IS_WRITEABLE($param1); // NOK
        is_writable($param1); // OK
    }
}