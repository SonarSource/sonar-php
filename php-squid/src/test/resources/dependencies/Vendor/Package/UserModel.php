<?php

namespace Vendor\Package;

use IRoot;
use Vendor\Common\BaseModel;
use Vendor\Errors\RuntimeError;

class UserModel extends BaseModel {
    private $user;

    public function __construct(array $user) {
        if (empty($user)) {
            throw new RuntimeError('Empty user');
        }
        parent::__construct();
        $this->user = $user;
    }

    public function setInterface(IRoot $i) {
        $user['name'] = $i->getName();
    }

    public function setInterface(\IRoot $i) {
        $user['name'] = $i->getName();
    }
}
