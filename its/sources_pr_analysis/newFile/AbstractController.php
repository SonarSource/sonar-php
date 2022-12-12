<?php

namespace App\Controllers;

use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Tests\Logger;

class AbstractController
{
  public function foo(Response $response, Logger $logger)
  {
    $logger->debug("foo");
    return $response->getAge();
  }
}
