<?php
namespace Symfony\Bridge\Monolog\Handler;

use Monolog\Handler\AbstractProcessingHandler;

// @SuppressWarnings("S101")
class Invalid_Class_Name {
    abstract final function __construct(OutputInterface $output = null, $bubble = true, array $verbosityLevelMap = array())
    {
        parent::__construct(Logger::DEBUG, $bubble);

        if ($verbosityLevelMap) {
            $this->verbosityLevelMap = $verbosityLevelMap;
        }
    }
}
?>
