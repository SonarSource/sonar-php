<?php

use unexisting\package;
use another\unexisting\package;

/**
 * comment
 */
require_once 'Abstract.php';

// comment
class A extends B
{
    protected $a = 'someValue';
    public $b = 1000; #comment
}

echo <<<'EOT'
My name is "$name".
I am printing some {$foo->bar[1]}.
EOT;

?>
