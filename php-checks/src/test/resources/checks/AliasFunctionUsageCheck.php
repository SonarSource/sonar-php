<?php

class Foo
{
  public function chop($param1) {
  }

  public function bar($param1, $param2)
  {
    $chop = 1;               // OK
    $this->chop($param1);    // OK

    chop($param1);           // Noncompliant {{Replace this use of "chop" with "rtrim".}}
//  ^^^^
    CHOP($param1);           // Noncompliant {{Replace this use of "CHOP" with "rtrim".}}
    rtrim($param1);          // OK

    close($param1);          // Noncompliant {{Replace this use of "close" with "closedir".}}
    closedir($param1);       // OK

    doubleval($param1);      // Noncompliant {{Replace this use of "doubleval" with "floatval".}}
    floatval($param1);       // OK

    fputs($param1);          // Noncompliant {{Replace this use of "fputs" with "fwrite".}}
    fwrite($param1);         // OK

    ini_alter($param1);      // Noncompliant {{Replace this use of "ini_alter" with "ini_set".}}
    ini_set($param1);        // OK

    is_double($param1);      // Noncompliant {{Replace this use of "is_double" with "is_float".}}
    is_float($param1);       // OK

    is_integer($param1);     // Noncompliant {{Replace this use of "is_integer" with "is_int".}}
    is_int($param1);         // OK

    is_long($param1);        // Noncompliant {{Replace this use of "is_long" with "is_int".}}
    is_int($param1);         // OK

    is_real($param1);        // Noncompliant {{Replace this use of "is_real" with "is_float".}}
    is_float($param1);       // OK

    is_writeable($param1);   // Noncompliant {{Replace this use of "is_writeable" with "is_writable".}}
    is_writable($param1);    // OK

    join($param1);           // Noncompliant {{Replace this use of "join" with "implode".}}
    implode($param1);        // OK

    key_exists($param1, $param2);        // Noncompliant {{Replace this use of "key_exists" with "array_key_exists".}}
    array_key_exists($param1, $param2);  // OK

    magic_quotes_runtime($param1);       // Noncompliant {{Replace this use of "magic_quotes_runtime" with "set_magic_quotes_runtime".}}
    set_magic_quotes_runtime($param1);   // OK

    pos($param1);            // Noncompliant {{Replace this use of "pos" with "current".}}
    current($param1);        // OK

    show_source($param1);    // Noncompliant {{Replace this use of "show_source" with "highlight_file".}}
    highlight_file($param1); // OK

    sizeof($param1);         // Noncompliant {{Replace this use of "sizeof" with "count".}}
    count($param1);          // OK

    strchr($param1);         // Noncompliant {{Replace this use of "strchr" with "strstr".}}
    strstr($param1);         // OK
 }

}
