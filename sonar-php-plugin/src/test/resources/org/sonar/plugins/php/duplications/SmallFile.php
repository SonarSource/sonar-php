<?php

use unexisting.package;
use another.unexisting.package;

/**
 * @see Zend_Mail_Transport_Abstract
 */
require_once 'Zend/Mail/Transport/Abstract.php';

// A small class
class Zend_Mail extends Zend_Mime_Message
{
    protected _defaultTransport = null;
    protected _defaultTimeout = 1000; # the default timeout
    protected _defaultRate = 2.123e+15;
}

echo <<<'EOT'
My name is "$name". I am printing some $foo->foo.
Now, I am printing some {$foo->bar[1]}.
This should not print a capital 'A': \x41
EOT;

?>