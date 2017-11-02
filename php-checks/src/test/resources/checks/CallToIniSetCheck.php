<?php
  ini_set($name, $value);        // Noncompliant {{Move this configuration into a configuration file.}}
//^^^^^^^
  \ini_set($name, $value);       // Noncompliant
//^^^^^^^^
UseNS\ini_set($name, $value);  // OK
ini_set;                       // OK

myFunction($name, $value);     // OK
$x->ini_set($name, $value);    // OK
