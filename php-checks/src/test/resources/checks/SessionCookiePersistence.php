<?php
session_set_cookie_params(0);
session_set_cookie_params(42, "/path"); // NOK {{Pass "0" as first argument.}}
//                        ^^
session_set_cookie_params(42); // NOK
session_set_cookie_params($someValue);
session_set_cookie_params();
$x->session_set_cookie_params(42);
unrelated_function(42);