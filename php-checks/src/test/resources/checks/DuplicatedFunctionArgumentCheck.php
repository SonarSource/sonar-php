<?php

function compute($a, $a, $c) {          // Noncompliant {{Rename the duplicated function parameter "$a".}}
//              ^^^^^^^^^^^^
}

function compute($a, $a, $c, $b, $b) {  // Noncompliant {{Rename the duplicated function parameters "$a, $b".}}
//              ^^^^^^^^^^^^^^^^^^^^
}

function compute($a, $b, $c) {          // OK
}
