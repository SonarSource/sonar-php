<?php

class Sample2
{
    protected $i;

    public function __construct()
    {
        $this->i = 0;
    }

    public function getI()
    {
        return $this->i;
    }

    public function doNotUse()
    {
      getI();
    }

}
?>