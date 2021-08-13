<?php

define( 'M_CUSTOM_OPTION', 1 ); // Noncompliant {{Unknown WordPress option "M_CUSTOM_OPTION". Did you mean "MY_CUSTOM_OPTION"?}}
define( 'my_second_custom_option', 1 ); // Noncompliant {{Unknown WordPress option "my_second_custom_option". Did you mean "MY_SECOND_CUSTOM_OPTION"?}}

define( 'MY_THIRD_OPTION', 1 ); // Compliant
