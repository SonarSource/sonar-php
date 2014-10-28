<?php

call_user_method();          // NOK
define_syslog_variables();   // NOK

if (sql_regcase());          // NOK

call_user_func();            // OK
sql_regcase->func();         // OK
