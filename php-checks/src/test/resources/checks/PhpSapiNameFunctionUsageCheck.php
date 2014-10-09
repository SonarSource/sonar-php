<?php

doSomething(php_sapi_name());      // NOK

doSomething(PHP_SAPI);             // OK
doSomething($a->php_sapi_name());  // OK


