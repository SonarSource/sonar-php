<?php

class Example_Math
{
    public function sub($v1, $v2)
    {
        sleep(5);
        return ($v1 - $v2);
    }

    public function div($v1, $v2)
    {
        return ($v1 / $v2);
    }
    
    public function check($v1) {
        return $v1 > 1 ? true : false;
    }
}

