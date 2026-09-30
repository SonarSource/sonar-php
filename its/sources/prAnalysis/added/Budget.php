<?php

namespace App\Controllers;

use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Tests\Logger;

class Budget extends AbstractController
{
  public function foo(Response $response, Logger $logger)
  {
    return $response->getAge();
  }

  public function bar(Response $response, Logger $logger)
  {
    return $response->getAge();
  }
}
