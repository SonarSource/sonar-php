<?php

$i = 0;

loop:
  echo("i = $i");
  $i++;
  if ($i < 10){
    goto loop;       // Noncompliant {{Remove use of "goto" statement.}}
//  ^^^^
  }
