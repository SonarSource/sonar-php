<?php
namespace Symfony\Bridge\Monolog\Handler;

use Monolog\Handler\AbstractProcessingHandler;

class ConsoleHandler extends AbstractProcessingHandler implements EventSubscriberInterface
{
    /*
     * @var OutputInterface|null
     */
    private $output;

    /*
     * Constructor.
     *
     * @param OutputInterface|null $output            The console output to use (the handler remains disabled when passing null
     *                                                until the output is set, e.g. by using console events)
     * @param Boolean              $bubble            Whether the messages that are handled can bubble up the stack
     * @param array                $verbosityLevelMap Array that maps the OutputInterface verbosity to a minimum logging
     *                                                level (leave empty to use the default mapping)
     */
    public function __construct(OutputInterface $output = null, $bubble = true, array $verbosityLevelMap = array())
    {
        parent::__construct(Logger::DEBUG, $bubble);

        if ($verbosityLevelMap) {
            $this->verbosityLevelMap = $verbosityLevelMap;
        }
    }

    private function updateLevel()
    {
        if (null === $this->output || OutputInterface::VERBOSITY_QUIET === $verbosity = $this->output->getVerbosity()) {
            return false;
        }

        if (isset($this->verbosityLevelMap[$verbosity])) {
            $this->setLevel($this->verbosityLevelMap[$verbosity]);
        }

        return true;
    }
}

function helper($param) {
  if ($param != 0) {
    return $param;
  }
  return $param / 2;
}

if (true) {
  return;
}
