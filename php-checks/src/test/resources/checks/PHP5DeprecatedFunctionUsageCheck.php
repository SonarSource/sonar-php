<?php

call_user_method();          // NOK
define_syslog_variables();   // NOK

if (sql_regcase());          // NOK

setlocale('LC_ALL', "");     // NOK
setlocale("LC_ALL", "") ;    // NOK

call_user_func();            // OK
sql_regcase->func();         // OK
setlocale(LC_ALL, "");       // OK
setlocale("0", "") ;         // OK
setlocale();                 // OK
