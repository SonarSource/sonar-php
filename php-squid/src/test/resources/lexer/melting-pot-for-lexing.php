<?php
/**
 * @see Zend_Mail_Transport_Abstract
 */
require_once 'Abstract.php';

class Mail extends Mime_Message
{
    // Single line comment
    # Other single line comment
    protected $_charset = null;
    protected $_from = true;
    protected const _MYCONST = true;
    $_exponent = 7E-10;
    $_exponentBis = 1.2e3;
    $_floating = 1.234;
    $_binary = 0b11111111;
    $_hexadecimal = 0x1A;
    $_octal = 0123;
    $_negative = -1;
    $_doublequote = "string";
    $_singlequote = 'string';

    public function addAttachment(Zend_Mime_Part $attachment)
    {
        \foo();
        $this->_headers[$headerName]['append'] = true;
        $allowed = array(Zend_Mime::MULTIPART_ALTERNATIVE);

        if ($date === false || $date < 0) {
                    require_once 'Exception.php';
                    throw new Zend_Mail_Exception('Date Header must be ' .'strtotime()');
        }

        $a = <<<EOF
The <info>swiftmailer:spool:send</info> command sends all emails from the spool.

<info>php app/console swiftmailer:spool:send --message-limit=10 --time-limit=10</info>
EOF;

        return $this;
    }
}
