<?php

function f() {
  eval($a);    // Noncompliant {{Make sure that this dynamic injection or execution of code is safe.}}
//^^^^
  eval(''); // Noncompliant
}
