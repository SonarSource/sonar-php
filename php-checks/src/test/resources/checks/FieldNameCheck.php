<?php

class myClass {
  public $MyVariable;      // Noncompliant {{Rename this field "$MyVariable" to match the regular expression ^[a-z][a-zA-Z0-9]*$.}}
//       ^^^^^^^^^^^
  public $MyVariable = 1;  // Noncompliant
  public $myVariable;      // OK
}

class ConstructorPropertyPromotion {
  public function __construct(
    public string $myField,           // OK
    protected int $MyField,           // Noncompliant {{Rename this field "$MyField" to match the regular expression ^[a-z][a-zA-Z0-9]*$.}}
//                ^^^^^^^^
    private string $my_field,         // Noncompliant {{Rename this field "$my_field" to match the regular expression ^[a-z][a-zA-Z0-9]*$.}}
    public readonly string $myReadonly, // OK
    string $regularParam,             // OK - not a promoted property
    string $REGULAR_PARAM             // OK - not a promoted property, checked by S117
  ) {}
}
