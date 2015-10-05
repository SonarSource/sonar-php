<?php

doSomething(php_sapi_name());      // NOK {{Use the "PHP_SAPI" constant instead.}}

doSomething(PHP_SAPI);             // OK
doSomething($a->php_sapi_name());  // OK


