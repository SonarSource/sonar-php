<?php

class myClass {
  public $MyVariable;      // Noncompliant {{Rename this field "$MyVariable" to match the regular expression ^[a-z][a-zA-Z0-9]*$.}}
//       ^^^^^^^^^^^
  public $MyVariable = 1;  // Noncompliant
  public $myVariable;      // OK
}
