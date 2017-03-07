<?php

//php:S115 Types should be named in camel case
class Example_Math
{
    public function sub($v1, $v2)
    {
        // php:S2964 "sleep" should not be called
        sleep(5);
        return ($v1 - $v2);
    }

    public function div($v1, $v2)
    {
        return ($v1 / $v2);
    }

    public function check($v1) {
        // php:S1125:  Boolean literals should not be redundant
        return $v1 > 1 ? true
         : false; // NOSONAR
    }
}
