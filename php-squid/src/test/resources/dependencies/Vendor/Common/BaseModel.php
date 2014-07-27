<?php

namespace Vendor\Common;

class BaseModel {
    public function __construct() {
    }

    public function getObjectId() {
        return spl_object_hash($this);
    }
}
