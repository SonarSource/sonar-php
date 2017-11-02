<?php

doSomething(php_sapi_name());         // Noncompliant {{Use the "PHP_SAPI" constant instead.}}
//          ^^^^^^^^^^^^^

doSomething(UserNS\php_sapi_name());  // OK

doSomething(PHP_SAPI);                // OK
doSomething($a->php_sapi_name());     // OK


