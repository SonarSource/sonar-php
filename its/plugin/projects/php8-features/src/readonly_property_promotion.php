<?php

declare(strict_types=1);

namespace MyCoolNamespace\src;

class Domain
{
    public function __construct(readonly private string $prop) {}
}
