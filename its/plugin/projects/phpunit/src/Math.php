<?php

namespace SonarPhp\PhpUnit;

class Math
{
    public static function add($v1 , $v2): int
    {
        return ($v1 + $v2);
    }

    public static function sub($v1, $v2): int
    {
        return ($v1 - $v2);
    }

    public static function div($v1, $v2): float
    {
        return ($v1 / $v2);
    }
}

