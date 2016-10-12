<?php

function f() {
  eval($a);    // NOK {{Remove this use of the "eval" function.}}
//^^^^
}
