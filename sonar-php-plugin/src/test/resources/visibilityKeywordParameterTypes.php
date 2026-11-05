<?php

namespace App\Repository\Public;
class FooRepository {}

namespace App\Repository\Private;
class FooRepository {}

namespace App\Repository\Protected;
class FooRepository {}

namespace App\Http;

use App\Repository\Public;
use App\Repository\Private;
use App\Repository\Protected;

class FooController
{
    public function foo(Public\FooRepository $public, Private\FooRepository $private, Protected\FooRepository $protected): void
    {

    }
}
