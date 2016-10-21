<?php

class Example_Math
{

    public function add($v1 , $v2, $v3)
    {
        return ($v1 + $v2);
    }

    public function sub($v1, $v2)
    {
        return ($v1 - $v2);
    }

    public function div($v1, $v2)
    {
        return ($v1 / $v2);
    }
}

function duplicate($p1, $p2, $p3) {
   $a = $p1 + $p2;
   $b = doSomething($p3);

   while ($p1 < $p2) {
     $p1++;
   }

  return valueOf($a) - $b;
}

