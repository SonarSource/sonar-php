<?php

namespace N1;

    use Cake\Auth\BaseAuthorize;
    use Cake\Controller\Controller;

    abstract class MyAuthorize1 extends \Cake\Auth\BaseAuthorize { // Noncompliant {{Make sure that Permissions are controlled safely here.}}
//                                      ^^^^^^^^^^^^^^^^^^^^^^^^
    }

    abstract class MyAuthorize1 extends \cake\auth\baseauthorize { // Noncompliant
    }

    abstract class MyAuthorize2 extends BaseAuthorize { // Noncompliant
//                                      ^^^^^^^^^^^^^
    }

    abstract class MyController extends Controller {
        public function isAuthorized($user) { // Noncompliant
//                      ^^^^^^^^^^^^
            return false;
        }
    }

    function isAuthorized($user) { } // OK

    class B extends Controller {
        public $isAuthorized = 1+2; // OK - not a function

        public function isOk($user) { }
    }

    class C {
        function isAuthorized($user) { } // OK
    }

    new class($collection) extends BaseAuthorize implements SomeInterface { }; // Noncompliant
//                                 ^^^^^^^^^^^^^

    new class($collection) extends Controller { }; // Ok

    new class() extends Controller {
        public function isAuthorized($user) { // Noncompliant
        }
    };

    new class() extends Controller {
        public function isauthorized($user) { // Noncompliant
        }
    };

namespace Cake;

    abstract class MyAuthorize extends \Cake\Auth\BaseAuthorize { // Noncompliant
//                                     ^^^^^^^^^^^^^^^^^^^^^^^^
    }

    abstract class MyAuthorize extends Cake\Auth\BaseAuthorize { // Ok - resolves to \Cake\Cake\Auth\BaseAuthorize
    }

    abstract class MyAuthorize extends Auth\BaseAuthorize { // Noncompliant
//                                     ^^^^^^^^^^^^^^^^^^
    }
