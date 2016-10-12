<?php

class myClass {
  public $MyVariable;      // NOK {{Rename this field "$MyVariable" to match the regular expression ^[a-z][a-zA-Z0-9]*$.}}
//       ^^^^^^^^^^^
  public $MyVariable = 1;  // NOK
  public $myVariable;      // OK
}
