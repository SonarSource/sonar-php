<?php

namespace Vendor\Package;

use Vendor\Common\UnitTest as TestCase;
use Vendor\Errors as ErrorsNS;

class UnitTest extends TestCase implements PackageInterface {

    public function test() {
        throw new ErrorsNS\RuntimeError(\RuntimeError::ERROR_CODE);
    }
}

