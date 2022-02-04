<?php

namespace checks\regex;

class MultipleWhitespaceCheck
{
  function noncompliant($input) {
    preg_match('/Hello,   world!/', $input);          // Noncompliant {{Replace spaces with quantifier `{3}`.}}
    //                  ^^
    preg_match('/Hello,  world!/', $input);           // Noncompliant {{Replace spaces with quantifier `{2}`.}}
    //                  ^
    preg_match('/Hello, world!   /', $input);         // Noncompliant
  }

  function compliant($input) {
    preg_match('/Hello , world!/', $input);
    preg_match('/Hello, {3}world!/', $input);
    preg_match('/Hello,   world!/x', $input);
  }
}
