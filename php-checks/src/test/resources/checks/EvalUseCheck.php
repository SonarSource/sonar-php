<?php

function f() {
  eval($a);    // Noncompliant {{Remove this use of the "eval" function.}}
//^^^^
}
