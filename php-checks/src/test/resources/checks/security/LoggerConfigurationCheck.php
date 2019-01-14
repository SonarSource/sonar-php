<?php

function configure_logging() {
  error_reporting(E_RECOVERABLE_ERROR);// Noncompliant {{Make sure that this logger's configuration is safe.}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  Error_Reporting(32);// Noncompliant

  ini_set('docref_root', '1');// Noncompliant
//^^^^^^^^^^^^^^^^^^^^^^^^^^^
  ini_set('display_errors', '1');// Noncompliant
  INI_SET('display_startup_errors', '1');// Noncompliant
  ini_set('error_log', "path/to/logfile1");// Noncompliant
  ini_set('error_log', "path/to/logfile2");// Noncompliant
  ini_set('error_reporting', E_PARSE );// Noncompliant
  ini_set('error_reporting', 64);// Noncompliant
  ini_set('log_errors', '0');// Noncompliant
  ini_set('log_errors_max_length', '512');// Noncompliant
  ini_set('ignore_repeated_errors', '1');// Noncompliant
  ini_set('ignore_repeated_source', '1');// Noncompliant
  ini_set('track_errors', '0');// Noncompliant

  ini_alter('docref_root', 'anythingElse');// Noncompliant
  INI_ALTER('display_errors', 'anythingElse');// Noncompliant
  ini_alter('display_startup_errors', 'anythingElse');// Noncompliant
  ini_alter('error_log', "path/to/logfile1");// Noncompliant
  ini_alter('error_log', "path/to/logfile2");// Noncompliant
  ini_alter('error_reporting', 1);// Noncompliant
  ini_alter('log_errors', '3');// Noncompliant
  ini_alter('log_errors_max_length', '2000');// Noncompliant
  ini_alter('ignore_repeated_errors', '2');// Noncompliant
  ini_alter('ignore_repeated_source', '5');// Noncompliant
  ini_alter('track_errors', NULL);// Noncompliant

// Followed settings are OK
  ini_set('docref_root', '0');
  ini_set('display_errors', '0');
  ini_set('display_startup_errors', '0');

  error_reporting(); // Ok - returns current level of error_reporting
  error_reporting(E_ALL);
  error_reporting(32767);
  error_reporting(-1);
  ini_set('error_reporting', E_ALL);
  ini_set('error_reporting', 32767);
  ini_set('error_reporting', -1);

  ini_set('log_errors', '1');
  ini_set('log_errors_max_length', '0');
  ini_set('ignore_repeated_errors', '0');
  ini_set('ignore_repeated_source', '0');
  ini_set('track_errors', '1');

// For coverage
  log_errors(2);
  ini_set('abcde', 2);
  ini_set(2, 2);
  ini_set(NULL, NULL);
  ini_alter["abc"]('track_errors', NULL);
  error_reporting["abc"](32);
  other_methods('log_errors', '3');
  ini_set('track_errors', '0', 'abc'); // Ok - should not happen as function takes only 2 arguments. We do not raise issue in such case
}

abstract class MyLogger1 implements \Psr\Log\LoggerInterface { // Noncompliant
//                                  ^^^^^^^^^^^^^^^^^^^^^^^^
}

abstract class MyLogger2 implements Abc, \Psr\Log\LoggerInterface { // Noncompliant
//                                       ^^^^^^^^^^^^^^^^^^^^^^^^
}

abstract class MyLogger3 extends AbstractLogger { // Ok - not resolved to '\Psr\Log\AbstractLogger'
    // ...
}

use psr\log\abstractlogger;

abstract class MyLogger4 extends AbstractLogger { // Noncompliant
//                               ^^^^^^^^^^^^^^
}


abstract class MyLogger5 extends OkLogger { // Ok
    // ...
}

abstract class MyLogger6 {
    use \Psr\Log\LoggerTrait;// Noncompliant
//      ^^^^^^^^^^^^^^^^^^^^
}

use \Psr\Log;

abstract class MyLogger7 {
    use A, B, Log\LoggerTrait;// Noncompliant
//            ^^^^^^^^^^^^^^^
}

new class() extends abstractlogger { }; // Noncompliant
new class() implements Log\LoggerInterface { }; // Noncompliant
